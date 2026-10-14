package com.myopty.billing.dto;

/**
 * The success envelope every billing endpoint returns: the repository's
 * {@code { "success": true, "data": ... }} shape, matching the order module's and
 * the auth endpoints'.
 *
 * <p>A local copy rather than a shared one, for the same reason order keeps its
 * own: a common envelope is a shared-module decision, and a module does not get to
 * make it by importing a sibling's type.
 */
public record ApiResponse<T>(boolean success, T data) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data);
    }
}
