package com.myopty.catalog.service;

import com.myopty.catalog.config.LowStockAlertProperties;
import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.model.StockItemType;
import com.myopty.catalog.model.StockStatus;
import com.myopty.catalog.repository.InventoryReportRepository;
import com.myopty.catalog.repository.StockRow;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Decides when a sale has dropped an item into a band the shop must act on, and
 * tells the shop about it.
 *
 * <p>A move only alerts when it <em>crosses</em> the line — the item was in stock
 * before the sale and is low or out after it. An item that was already low stays
 * low without a second email for every unit sold; the alert is news, not a
 * running tally, and the in-app banner is what shows the current state.
 *
 * <p>The send is deferred to after the order's transaction commits. The alert
 * describes a sale that actually happened; if the order rolls back — a later lens
 * move failed, the transaction was refused — no sale occurred and an email would
 * be a lie the shop cannot undo. Deferring also keeps a slow mail server off the
 * customer's request: the commit returns, then the alert goes out.
 *
 * <p>Delivery is best-effort. A sale is the source of truth and must not fail
 * because the shop's mailbox is unreachable, so a send error is logged and
 * swallowed rather than raised to the order.
 */
@Service
public class LowStockAlerts {

    private static final Logger log = LoggerFactory.getLogger(LowStockAlerts.class);

    private final InventoryReportRepository inventory;
    private final LowStockAlertSender sender;
    private final LowStockAlertProperties properties;

    public LowStockAlerts(
            InventoryReportRepository inventory, LowStockAlertSender sender, LowStockAlertProperties properties) {
        this.inventory = inventory;
        this.sender = sender;
        this.properties = properties;
    }

    /**
     * The item to alert on, if this move crossed it into a low or out band.
     *
     * <p>{@code stockBefore} is the level the item held before the move; the row
     * read here carries the level after it, because the read happens inside the
     * same transaction as the decrement. Empty when the item is gone or
     * discontinued, or when the move did not cross.
     */
    public Optional<AvailabilityItemResponse> classify(StockItemType itemType, long itemId, int stockBefore) {
        Optional<StockRow> row =
                itemType == StockItemType.FRAME ? inventory.activeFrame(itemId) : inventory.activeLens(itemId);
        return row.filter(item -> crossed(item, stockBefore)).map(AvailabilityItemResponse::from);
    }

    private static boolean crossed(StockRow item, int stockBefore) {
        int threshold = item.lowStockThreshold();
        return StockStatus.of(stockBefore, threshold) == StockStatus.IN_STOCK
                && StockStatus.of(item.stockQty(), threshold) != StockStatus.IN_STOCK;
    }

    /**
     * Delivers the crossings once the surrounding transaction commits, or
     * immediately when there is no transaction to wait for (a direct unit call).
     * An empty list is a no-op, which is the common case.
     */
    public void sendAfterCommit(List<AvailabilityItemResponse> crossings) {
        if (crossings.isEmpty()) {
            return;
        }
        List<AvailabilityItemResponse> snapshot = List.copyOf(crossings);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch(snapshot);
                }
            });
        } else {
            dispatch(snapshot);
        }
    }

    private void dispatch(List<AvailabilityItemResponse> crossings) {
        if (!properties.isEnabled()) {
            return;
        }
        String recipient = properties.getRecipientEmail();
        if (recipient == null || recipient.isBlank()) {
            log.debug(
                    "{} item(s) crossed into low stock, but no alert recipient is configured; not emailing.",
                    crossings.size());
            return;
        }
        try {
            sender.send(crossings, recipient);
        } catch (RuntimeException exception) {
            log.warn("Could not send low-stock alert to {}: {}", recipient, exception.getMessage());
        }
    }
}
