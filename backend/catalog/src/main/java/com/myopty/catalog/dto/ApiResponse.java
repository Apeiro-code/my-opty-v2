package com.myopty.catalog.dto;

/**
 * The success envelope from the README's API guidelines: {@code { "success": true,
 * "data": { ... } }}, the shape {@code shared}'s {@code AuthResponse} and the
 * order module's endpoints use.
 *
 * <p>Generic because the catalog's endpoints return several different payloads,
 * and a catalog-local copy rather than a shared one because only one module at a
 * time has needed a generic wrapper — a shared type is a team decision, not a
 * convenience one module takes on its own.
 */
public record ApiResponse<T>(boolean success, T data) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data);
    }
}
