package com.myopty.catalog.service;

import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.dto.AvailabilityReportResponse;
import com.myopty.catalog.model.StockStatus;
import com.myopty.catalog.repository.InventoryReportRepository;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the shop's real-time availability report: the answer is computed at
 * call time from the live shelf counts, not read from a stored snapshot, so the
 * client plans reorders against where stock is now.
 *
 * <p>Each row keeps its band ({@link StockStatus}) so the page can group and
 * colour the table, and the three counts are summed once rather than replayed
 * from each list entry. Inactive items are absent — see
 * {@link InventoryReportRepository}.
 */
@Service
public class InventoryReportService {

    private final InventoryReportRepository inventory;

    public InventoryReportService(InventoryReportRepository inventory) {
        this.inventory = inventory;
    }

    @Transactional(readOnly = true)
    public AvailabilityReportResponse generate() {
        List<AvailabilityItemResponse> items = Stream.concat(
                        inventory.activeFrames().stream(), inventory.activeLenses().stream())
                .map(AvailabilityItemResponse::from)
                .toList();
        int inStock = count(items, StockStatus.IN_STOCK);
        int lowStock = count(items, StockStatus.LOW_STOCK);
        int outOfStock = count(items, StockStatus.OUT_OF_STOCK);
        return new AvailabilityReportResponse(items.size(), inStock, lowStock, outOfStock, items);
    }

    /**
     * The items the shop must act on now: everything not in stock, low first so
     * the empty shelves read before the thinning ones. This is the same live view
     * as {@link #generate()} narrowed to the two problem bands, which is what the
     * in-app banner shows — it is the current state, not the email the sale sent.
     */
    @Transactional(readOnly = true)
    public List<AvailabilityItemResponse> alerts() {
        return generate().items().stream()
                .filter(item -> item.status() != StockStatus.IN_STOCK)
                .sorted((left, right) -> Integer.compare(rank(left.status()), rank(right.status())))
                .toList();
    }

    /** Out of stock sorts before low stock. */
    private static int rank(StockStatus status) {
        return status == StockStatus.OUT_OF_STOCK ? 0 : 1;
    }

    private static int count(List<AvailabilityItemResponse> items, StockStatus status) {
        return (int) items.stream().filter(item -> item.status() == status).count();
    }
}
