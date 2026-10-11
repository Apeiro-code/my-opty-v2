package com.myopty.catalog.model;

/**
 * Where an item sits against its shelf stock. Mirrors V6's report bands:
 * {@code 0} is {@link #OUT_OF_STOCK}, anything at or below the item's
 * low-stock threshold is {@link #LOW_STOCK}, everything above is
 * {@link #IN_STOCK}.
 */
public enum StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK;

    public static StockStatus of(int stockQty, int lowStockThreshold) {
        if (stockQty <= 0) {
            return OUT_OF_STOCK;
        }
        return stockQty <= lowStockThreshold ? LOW_STOCK : IN_STOCK;
    }
}
