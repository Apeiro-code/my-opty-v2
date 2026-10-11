package com.myopty.contracts.catalog;

/**
 * One order's demand on the catalog's stock: the frame and lens it takes off the
 * shelf, and how many of each.
 *
 * <p>{@code frameId} is null for a lenses-only order, mirroring
 * {@code progressive_order}. {@code orderNumber} is carried so the catalog can
 * write a human-readable reference into its stock audit log without having to
 * read the order module's tables.
 */
public record StockReservation(long orderId, String orderNumber, Long frameId, Long lensId, int quantity) {}
