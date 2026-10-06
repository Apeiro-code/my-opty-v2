package com.myopty.shared.auth.dto;

/**
 * The failure half of the response envelope from the README's API guidelines:
 * {@code { "success": false, "error": { "code", "message" } }}.
 *
 * <p>One shape for every refusal — the ones the controller writes (bad
 * password, inactive account) and the ones the security filter chain writes
 * before a controller is ever reached (not authenticated, not permitted). A
 * client can branch on {@code error.code} without caring which of the two
 * layers produced it.
 */
public record ApiError(boolean success, Error error) {

    public record Error(String code, String message) {}

    public static ApiError of(String code, String message) {
        return new ApiError(false, new Error(code, message));
    }
}
