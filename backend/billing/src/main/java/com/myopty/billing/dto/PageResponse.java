package com.myopty.billing.dto;

import java.util.List;

/**
 * One page of a list result, with the paging arithmetic the client needs to draw
 * a pager.
 *
 * <p>The repository's envelope is {@code { success, data }} with nowhere for a
 * sibling {@code meta}, so the paging fields ride inside {@code data} alongside the
 * items. {@code totalPages} is derived here so the client does not recompute it,
 * and — because it is derived — a page beyond the end reports the real count
 * rather than pretending the list is empty for no reason.
 */
public record PageResponse<T>(List<T> items, int page, int limit, long totalItems, int totalPages) {

    public static <T> PageResponse<T> of(List<T> items, int page, int limit, long totalItems) {
        int totalPages = limit <= 0 ? 0 : (int) ((totalItems + limit - 1) / limit);
        return new PageResponse<>(items, page, limit, totalItems, totalPages);
    }
}
