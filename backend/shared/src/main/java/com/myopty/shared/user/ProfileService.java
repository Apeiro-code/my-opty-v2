package com.myopty.shared.user;

import com.myopty.shared.auth.dto.AuthenticatedUser;
import com.myopty.shared.user.dto.UpdateProfileRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Reads and updates the signed-in customer's profile.
 *
 * <p>It touches two rows: {@code full_name} and {@code phone} on {@code app_user},
 * and the customer's {@code address} / {@code preferred_contact}, which this class
 * upserts into {@code customer_profile}. Email is left alone — it is the login
 * identity, and changing it is a verification story.
 *
 * <p>The update refreshes the session's principal, because the principal is what
 * the filter chain stored at login and {@code GET /api/auth/me} reads it back: a
 * row changed in the database but not in the session would leave the header
 * showing the old name until the next sign-in. Saving the rebuilt authentication
 * is the same call the login endpoint makes when it first establishes one.
 */
@Service
public class ProfileService {

    private final AppUserRepository users;
    private final CustomerProfileRepository profiles;
    private final SecurityContextRepository securityContextRepository;

    public ProfileService(
            AppUserRepository users,
            CustomerProfileRepository profiles,
            SecurityContextRepository securityContextRepository) {
        this.users = users;
        this.profiles = profiles;
        this.securityContextRepository = securityContextRepository;
    }

    /** The signed-in user and their profile, as the auth endpoints report it. */
    @Transactional(readOnly = true)
    public AuthenticatedUser read(Long userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
        CustomerProfile profile = profiles.findById(userId).orElse(null);
        return AuthenticatedUser.from(user, profile);
    }

    @Transactional
    public AuthenticatedUser update(
            Long userId,
            UpdateProfileRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        String phone = blankToNull(request.phone());
        if (phone != null && !phone.equals(user.getPhone()) && users.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PHONE_EXISTS");
        }

        AppUser updated = new AppUser(
                user.getId(),
                user.getEmail(),
                phone,
                user.getPassword(),
                request.name().trim(),
                user.getRole(),
                user.getStatus(),
                user.isEmailVerified(),
                user.isPhoneVerified());
        updated = users.save(updated);

        String address = blankToNull(request.address());
        profiles.upsert(
                userId,
                address,
                request.preferredContact() == null
                        ? null
                        : request.preferredContact().name());

        refreshPrincipal(updated, httpRequest, httpResponse);

        return AuthenticatedUser.from(updated, new CustomerProfile(userId, address, request.preferredContact()));
    }

    private void refreshPrincipal(AppUser user, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current == null) {
            return;
        }
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, current.getCredentials(), user.getAuthorities()));
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpRequest, httpResponse);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
