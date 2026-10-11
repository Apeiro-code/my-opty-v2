package com.myopty.catalog.service;

import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.model.StockChangeType;
import com.myopty.catalog.model.StockEntry;
import com.myopty.catalog.model.StockItemType;
import com.myopty.catalog.repository.FrameRepository;
import com.myopty.catalog.repository.LensRepository;
import com.myopty.catalog.repository.StockEntryRepository;
import com.myopty.contracts.catalog.InsufficientStockException;
import com.myopty.contracts.catalog.StockManager;
import com.myopty.contracts.catalog.StockReservation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The catalog's half of the order story: takes a placed order's frame and lens
 * off the shelf, and puts them back if the order is rejected.
 *
 * <p>It implements {@link StockManager} so the order module can call it through
 * {@code contracts} without depending on this module. The moves are conditional
 * single-statement updates (see {@link FrameRepository}) so concurrent orders
 * cannot oversell a shelf, and each move appends a {@code stock_entry} row so the
 * change is auditable without replaying the whole log.
 *
 * <p>Everything runs in the caller's transaction (the order's), so an order that
 * cannot be filled rolls back whole — the customer is not left with an order the
 * shop cannot supply. {@code created_by} stays null because this is the system
 * acting on a customer's order, not a logged-in user editing the catalogue.
 *
 * <p>A sale that drops an item across its low-stock threshold is collected as the
 * moves are made and handed to {@link LowStockAlerts}, which emails the shop once
 * the order's transaction commits. An alert never fails a sale.
 */
@Service
public class StockService implements StockManager {

    private final FrameRepository frames;
    private final LensRepository lenses;
    private final StockEntryRepository entries;
    private final LowStockAlerts alerts;

    public StockService(
            FrameRepository frames, LensRepository lenses, StockEntryRepository entries, LowStockAlerts alerts) {
        this.frames = frames;
        this.lenses = lenses;
        this.entries = entries;
        this.alerts = alerts;
    }

    /** A frame is optional on an optical order; a lens is not, so only the frame move is conditional. */
    @Transactional
    @Override
    public void reserve(StockReservation reservation) {
        int quantity = reservation.quantity();
        List<AvailabilityItemResponse> crossings = new ArrayList<>();
        if (reservation.frameId() != null) {
            take(StockItemType.FRAME, reservation.frameId(), quantity, reservation, crossings);
        }
        take(StockItemType.LENS, reservation.lensId(), quantity, reservation, crossings);
        alerts.sendAfterCommit(crossings);
    }

    /** A rejection reverses a reservation, so both moves simply add the units back. */
    @Transactional
    @Override
    public void release(StockReservation reservation) {
        int quantity = reservation.quantity();
        if (reservation.frameId() != null) {
            give(StockItemType.FRAME, reservation.frameId(), quantity, reservation);
        }
        give(StockItemType.LENS, reservation.lensId(), quantity, reservation);
    }

    private void take(
            StockItemType itemType,
            long itemId,
            int quantity,
            StockReservation reservation,
            List<AvailabilityItemResponse> crossings) {
        if (decrement(itemType, itemId, quantity) == 0) {
            throw new InsufficientStockException(itemType.name(), itemId);
        }
        int stockAfter = stockAfter(itemType, itemId);
        record(itemType, itemId, StockChangeType.SALE, -quantity, stockAfter, reservation);
        alerts.classify(itemType, itemId, stockAfter + quantity).ifPresent(crossings::add);
    }

    private void give(StockItemType itemType, long itemId, int quantity, StockReservation reservation) {
        increment(itemType, itemId, quantity);
        record(itemType, itemId, StockChangeType.RETURN, quantity, stockAfter(itemType, itemId), reservation);
    }

    private int decrement(StockItemType itemType, long itemId, int quantity) {
        return itemType == StockItemType.FRAME
                ? frames.decrementStock(itemId, quantity)
                : lenses.decrementStock(itemId, quantity);
    }

    private void increment(StockItemType itemType, long itemId, int quantity) {
        if (itemType == StockItemType.FRAME) {
            frames.incrementStock(itemId, quantity);
        } else {
            lenses.incrementStock(itemId, quantity);
        }
    }

    private int stockAfter(StockItemType itemType, long itemId) {
        return itemType == StockItemType.FRAME
                ? frames.findById(itemId).orElseThrow().getStockQty()
                : lenses.findById(itemId).orElseThrow().getStockQty();
    }

    private void record(
            StockItemType itemType,
            long itemId,
            StockChangeType changeType,
            int quantityChange,
            int stockAfter,
            StockReservation reservation) {
        StockEntry entry = new StockEntry();
        entry.setItemType(itemType);
        entry.setItemId(itemId);
        entry.setChangeType(changeType);
        entry.setQuantityChange(quantityChange);
        entry.setStockAfter(stockAfter);
        entry.setReference(reservation.orderNumber());
        entry.setNote("Order " + reservation.orderNumber());
        entries.save(entry);
    }
}
