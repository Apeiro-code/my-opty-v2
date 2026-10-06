package com.myopty.shared.auth.dto;

import com.myopty.shared.user.AppUser;

/**
 * The identity a session carries, as both {@code /api/auth/login} and
 * {@code /api/auth/me} report it.
 *
 * <p>Deliberately small: id, email, name, role. Passwords never appear on the
 * way out, and neither do verification flags or status — a session only exists
 * for an {@code ACTIVE} account, so those are settled facts by the time this is
 * serialized.
 *
 * @param role the plain enum name ({@code CUSTOMER} / {@code CLIENT}), never the
 *     {@code ROLE_} prefix the security layer uses internally; HTTP clients
 *     should not have to know the implementation detail of authority strings
 */
public record AuthenticatedUser(long id, String email, String fullName, String role) {

    public static AuthenticatedUser from(AppUser user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name());
    }
}
