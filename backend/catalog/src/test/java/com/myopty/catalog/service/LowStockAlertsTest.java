package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.myopty.catalog.config.LowStockAlertProperties;
import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.model.StockItemType;
import com.myopty.catalog.model.StockStatus;
import com.myopty.catalog.repository.InventoryReportRepository;
import com.myopty.catalog.repository.StockRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The low-stock rule, without a database or an SMTP server: which moves count as
 * a crossing, and what the alerter does with the crossing it finds.
 *
 * <p>The repository is a stub for the single-item read and the sender records the
 * call, because what is under test is the crossing rule and the delivery policy
 * (disabled, no recipient, a failing sender) — not the SQL or the SMTP handshake,
 * which the integration tests cover.
 */
class LowStockAlertsTest {

    private final StubInventory inventory = new StubInventory();
    private final RecordingSender sender = new RecordingSender();
    private final LowStockAlertProperties properties = new LowStockAlertProperties();
    private final LowStockAlerts alerts = new LowStockAlerts(inventory, sender, properties);

    @Test
    void aMoveFromAboveTheThresholdIntoLowStockIsACrossing() {
        inventory.lens(2L, "Essential 1.50", 4, 5);

        Optional<AvailabilityItemResponse> crossed = alerts.classify(StockItemType.LENS, 2L, 6);

        assertThat(crossed).hasValueSatisfying(item -> {
            assertThat(item.stockQty()).isEqualTo(4);
            assertThat(item.status()).isEqualTo(StockStatus.LOW_STOCK);
        });
    }

    @Test
    void aMoveThatEmptiesTheShelfIsACrossing() {
        inventory.lens(2L, "Essential 1.50", 0, 5);

        assertThat(alerts.classify(StockItemType.LENS, 2L, 6))
                .hasValueSatisfying(item -> assertThat(item.status()).isEqualTo(StockStatus.OUT_OF_STOCK));
    }

    @Test
    void aMoveThatStaysInStockIsNotACrossing() {
        inventory.lens(2L, "Essential 1.50", 28, 5);

        assertThat(alerts.classify(StockItemType.LENS, 2L, 30)).isEmpty();
    }

    @Test
    void aMoveOfAnItemAlreadyLowIsNotACrossing() {
        inventory.lens(2L, "Essential 1.50", 3, 5);

        assertThat(alerts.classify(StockItemType.LENS, 2L, 4)).isEmpty();
    }

    @Test
    void anUnknownItemIsNotACrossing() {
        assertThat(alerts.classify(StockItemType.FRAME, 99L, 6)).isEmpty();
    }

    @Test
    void aCrossingIsDeliveredToTheConfiguredRecipient() {
        properties.setRecipientEmail("shop@myopty.local");
        List<AvailabilityItemResponse> items = List.of(item());

        alerts.sendAfterCommit(items);

        assertThat(sender.recipients).containsExactly("shop@myopty.local");
        assertThat(sender.items).containsExactlyElementsOf(items);
    }

    @Test
    void nothingIsSentWhenNoRecipientIsConfigured() {
        properties.setRecipientEmail("   ");

        alerts.sendAfterCommit(List.of(item()));

        assertThat(sender.recipients).isEmpty();
    }

    @Test
    void nothingIsSentWhenAlertsAreDisabled() {
        properties.setEnabled(false);
        properties.setRecipientEmail("shop@myopty.local");

        alerts.sendAfterCommit(List.of(item()));

        assertThat(sender.recipients).isEmpty();
    }

    @Test
    void noCrossingMeansNoSend() {
        properties.setRecipientEmail("shop@myopty.local");

        alerts.sendAfterCommit(List.of());

        assertThat(sender.recipients).isEmpty();
    }

    @Test
    void aFailingSenderDoesNotPropagate() {
        properties.setRecipientEmail("shop@myopty.local");
        sender.failWith = new IllegalStateException("SMTP is down");

        assertThatCode(() -> alerts.sendAfterCommit(List.of(item()))).doesNotThrowAnyException();
    }

    private static AvailabilityItemResponse item() {
        return new AvailabilityItemResponse("LENS", 2L, "Essential 1.50", 4, 5, StockStatus.LOW_STOCK);
    }

    private static final class StubInventory extends InventoryReportRepository {

        private StockRow lens;

        StubInventory() {
            super(null);
        }

        void lens(long id, String name, int stockQty, int threshold) {
            this.lens = new StockRow("LENS", id, name, stockQty, threshold);
        }

        @Override
        public Optional<StockRow> activeFrame(long id) {
            return Optional.empty();
        }

        @Override
        public Optional<StockRow> activeLens(long id) {
            return Optional.ofNullable(lens).filter(row -> row.itemId() == id);
        }
    }

    private static final class RecordingSender implements LowStockAlertSender {

        private final List<String> recipients = new ArrayList<>();
        private final List<AvailabilityItemResponse> items = new ArrayList<>();
        private RuntimeException failWith;

        @Override
        public void send(List<AvailabilityItemResponse> items, String recipientEmail) {
            if (failWith != null) {
                throw failWith;
            }
            recipients.add(recipientEmail);
            this.items.addAll(items);
        }
    }
}
