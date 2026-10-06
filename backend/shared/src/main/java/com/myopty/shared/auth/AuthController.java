package com.myopty.shared.auth;

import com.myopty.shared.auth.dto.ApiError;
import com.myopty.shared.auth.dto.AuthResponse;
import com.myopty.shared.auth.dto.AuthenticatedUser;
import com.myopty.shared.auth.dto.LoginRequest;
import com.myopty.shared.user.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The session's lifecycle: establish it, report it, end it.
 *
 * <p>Three endpoints are enough because Spring Security already owns everything
 * around them — deciding which paths need a session (see {@link SecurityConfig}),
 * storing the context that {@link #login} creates, and refusing anonymous
 * requests with the JSON entry point. This controller only performs the
 * authentication itself and describes the result in the repository's response
 * envelope.
 *
 * <p>Failure messages are deliberately uniform: a wrong password and an unknown
 * email produce the same 401, so the endpoint cannot be used to enumerate
 * accounts, and the disabled account gets its own code only because the person
 * holding the right password deserves to know why they still cannot get in.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Exchanges an email/password pair for a session.
     *
     * <p>Any session that predates authentication is discarded first: a session
     * identifier an attacker might have planted must not survive the moment the
     * identity is established, so the new identity always starts in a session
     * that did not exist before the password was checked.
     */
    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        Authentication authenticated = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        HttpSession existing = httpRequest.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        // The explicit save is not optional: Spring Security persists the
        // context only when something asks it to, and that something is the
        // filter chain's repository — without this call the login would succeed
        // and every following request would arrive anonymous.
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpRequest, httpResponse);

        AppUser user = (AppUser) authenticated.getPrincipal();
        return AuthResponse.of(AuthenticatedUser.from(user));
    }

    /**
     * Who the current session says the caller is. Anonymous callers never reach
     * this method — the filter chain answers them first with a 401 — so the
     * principal is never null here.
     */
    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal AppUser user) {
        return AuthResponse.of(AuthenticatedUser.from(user));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError badCredentials(BadCredentialsException exception) {
        return ApiError.of("INVALID_CREDENTIALS", "Email or password is incorrect.");
    }

    /**
     * The password was right and the account still cannot come in. A distinct
     * code from {@link #badCredentials} on purpose: the person on the other end
     * holds valid credentials and deserves an answer they can act on, while
     * everyone else only learns that their pair was wrong.
     */
    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError disabled(DisabledException exception) {
        return ApiError.of("ACCOUNT_NOT_ACTIVE", "This account is not active.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidBody(MethodArgumentNotValidException exception) {
        return ApiError.of("VALIDATION_FAILED", "Email and password are required.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError unreadableBody(HttpMessageNotReadableException exception) {
        return ApiError.of("MALFORMED_REQUEST", "The request body is not valid JSON.");
    }
}
