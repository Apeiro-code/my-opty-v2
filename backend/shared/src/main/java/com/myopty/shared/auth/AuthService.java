package com.myopty.shared.auth;

import com.myopty.shared.auth.dto.AuthResponse;
import com.myopty.shared.auth.dto.AuthenticatedUser;
import com.myopty.shared.auth.dto.RegisterRequest;
import com.myopty.shared.auth.dto.ResendVerificationRequest;
import com.myopty.shared.auth.dto.VerifyEmailRequest;
import com.myopty.shared.auth.verification.EmailVerification;
import com.myopty.shared.auth.verification.EmailVerificationPurpose;
import com.myopty.shared.auth.verification.EmailVerificationRepository;
import com.myopty.shared.email.EmailService;
import com.myopty.shared.user.AccountStatus;
import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.AppUserRepository;
import com.myopty.shared.user.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserRepository users;
    private final EmailVerificationRepository verifications;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(
            AppUserRepository users,
            EmailVerificationRepository verifications,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.users = users;
        this.verifications = verifications;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public AuthResponse register(
            RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (users.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EMAIL_EXISTS");
        }
        if (users.existsByPhone(request.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PHONE_EXISTS");
        }

        AppUser user = new AppUser(
                null,
                request.email(),
                request.phone(),
                passwordEncoder.encode(request.password()),
                request.name(),
                Role.CUSTOMER,
                AccountStatus.PENDING_VERIFICATION,
                false,
                false);
        user = users.save(user);

        String code = generateCode();
        EmailVerification verification = new EmailVerification();
        verification.setUserId(user.getId());
        verification.setPurpose(EmailVerificationPurpose.EMAIL_VERIFICATION);
        verification.setCodeHash(hashCode(code));
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verification.setAttempts(0);
        verification.setUsed(false);
        verifications.save(verification);

        emailService.sendVerificationEmail(user.getEmail(), code);
        authenticate(user.getEmail(), request.password(), httpRequest, httpResponse);
        return AuthResponse.of(AuthenticatedUser.from(user, null));
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        AppUser user = users.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_OTP"));
        EmailVerification verification = verifications
                .findTopByUserIdAndPurposeOrderByIdDesc(user.getId(), EmailVerificationPurpose.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_OTP"));
        if (verification.isUsed() || verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED");
        }
        if (verification.getAttempts() >= 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP_ATTEMPTS_EXCEEDED");
        }
        verification.setAttempts(verification.getAttempts() + 1);
        verifications.save(verification);
        if (!hashCode(request.code()).equals(verification.getCodeHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_OTP");
        }
        verification.setUsed(true);
        verifications.save(verification);
        // activate user
        AppUser updated = new AppUser(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword(),
                user.getFullName(),
                user.getRole(),
                AccountStatus.ACTIVE,
                true,
                user.isPhoneVerified());
        users.save(updated);
    }

    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        AppUser user = users.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
        String code = generateCode();
        EmailVerification verification = new EmailVerification();
        verification.setUserId(user.getId());
        verification.setPurpose(EmailVerificationPurpose.EMAIL_VERIFICATION);
        verification.setCodeHash(hashCode(code));
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verification.setAttempts(0);
        verification.setUsed(false);
        verifications.save(verification);
        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    private void authenticate(
            String email, String password, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        HttpSession existing = httpRequest.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        Authentication authenticated =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpRequest, httpResponse);
    }

    private String generateCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private String hashCode(String code) {
        return passwordEncoder.encode(code);
    }
}
