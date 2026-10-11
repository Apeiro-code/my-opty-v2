package com.myopty.catalog.model;

/**
 * The two kinds of product a stock movement can touch. Mirrors the
 * {@code chk_stock_entry_item_type} constraint on {@code stock_entry} (V5).
 */
public enum StockItemType {
    FRAME,
    LENS
}
