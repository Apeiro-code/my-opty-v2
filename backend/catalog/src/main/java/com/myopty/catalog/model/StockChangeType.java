package com.myopty.catalog.model;

/**
 * Why a stock movement happened. Mirrors the {@code chk_stock_entry_change_type}
 * constraint on {@code stock_entry} (V5).
 *
 * <p>{@link #SALE} is written when an order takes units off the shelf and
 * {@link #RETURN} when a rejected order puts them back; new stock intake writes
 * {@link #NEW_STOCK} and a manual correction {@link #ADJUSTMENT}.
 */
public enum StockChangeType {
    SALE,
    RETURN,
    NEW_STOCK,
    ADJUSTMENT;
}
