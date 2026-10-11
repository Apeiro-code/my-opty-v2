package com.myopty.catalog.dto;

import com.myopty.catalog.model.StockStatus;
import com.myopty.catalog.repository.StockRow;

/**
 * One item in the availability report: which frame or lens it is, how many are
 * on the shelf now, the threshold below which that is a problem, and the band
 * the count falls into.
 */
public record AvailabilityItemResponse(
        String itemType, long itemId, String name, int stockQty, int lowStockThreshold, StockStatus status) {

    public static AvailabilityItemResponse from(StockRow row) {
        return new AvailabilityItemResponse(
                row.itemType(),
                row.itemId(),
                row.name(),
                row.stockQty(),
                row.lowStockThreshold(),
                StockStatus.of(row.stockQty(), row.lowStockThreshold()));
    }
}
