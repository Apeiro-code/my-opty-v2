package com.myopty.order.dto;

/**
 * The success envelope from the README's API guidelines: {@code { "success": true,
 * "data": { ... } }}, the shape {@code shared}'s {@code AuthResponse} uses for the
 * auth endpoints.
 *
 * <p>Generic because order's endpoints return several different payloads, and an
 * order-local copy rather than a shared one because only one module alive yet
 * needs a generic wrapper — a shared type is a team decision, not a convenience
 * one module takes on its own.
 */
public record ApiResponse<T>(boolean success, T data) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data);
    }
}
