package com.myopty.catalog.repository;

/**
 * One catalogue row as the availability report needs it: the live shelf count
 * and the threshold that decides whether that count is healthy.
 *
 * <p>{@code itemType} is {@code FRAME} or {@code LENS} — the report mixes the two
 * product tables into one view, and the client reorders by product, so the type
 * travels with the row. Read by {@link InventoryReportRepository}, never
 * persisted.
 */
public record StockRow(String itemType, long itemId, String name, int stockQty, int lowStockThreshold) {}
