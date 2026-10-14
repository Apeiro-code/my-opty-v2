package com.myopty.shared.auth.dto;

import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.CustomerProfile;

/**
 * The identity a session carries, as both {@code /api/auth/login} and
 * {@code /api/auth/me} report it.
 *
 * <p>Deliberately small: id, email, name, role, and the customer's editable
 * profile fields. Passwords never appear on the way out, and neither do
 * verification flags or status — a session only exists for an {@code ACTIVE}
 * account, so those are settled facts by the time this is serialized.
 *
 * <p>The profile fields ({@code phone}, {@code address}, {@code preferredContact})
 * ride along so the account page does not need a second request to fill its form.
 * They are null for a client, whose extra details live in {@code client_profile}
 * and belong to a different story.
 *
 * @param role the plain enum name ({@code CUSTOMER} / {@code CLIENT}), never the
 *     {@code ROLE_} prefix the security layer uses internally; HTTP clients
 *     should not have to know the implementation detail of authority strings
 */
public record AuthenticatedUser(
        long id, String email, String fullName, String role, String phone, String address, String preferredContact) {

    public static AuthenticatedUser from(AppUser user, CustomerProfile profile) {
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getPhone(),
                profile == null ? null : profile.getAddress(),
                profile == null || profile.getPreferredContact() == null
                        ? null
                        : profile.getPreferredContact().name());
    }
}
