package com.myopty.shared.auth.dto;

/**
 * The success envelope for the auth endpoints: {@code { "success": true,
 * "data": { ... } }}, as the README's API guidelines require.
 *
 * <p>A record rather than a {@code Map} so the shape is visible in review and
 * wrong keys are a compile error instead of a runtime surprise for whoever
 * writes the frontend's fetch wrapper.
 */
public record AuthResponse(boolean success, AuthenticatedUser data) {

    public static AuthResponse of(AuthenticatedUser user) {
        return new AuthResponse(true, user);
    }
}
