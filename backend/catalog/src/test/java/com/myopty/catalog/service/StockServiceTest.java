package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.catalog.config.LowStockAlertProperties;
import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.model.Frame;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.StockChangeType;
import com.myopty.catalog.model.StockEntry;
import com.myopty.catalog.model.StockItemType;
import com.myopty.catalog.model.StockStatus;
import com.myopty.catalog.repository.FrameRepository;
import com.myopty.catalog.repository.InventoryReportRepository;
import com.myopty.catalog.repository.LensRepository;
import com.myopty.catalog.repository.StockEntryRepository;
import com.myopty.catalog.repository.StockRow;
import com.myopty.contracts.catalog.InsufficientStockException;
import com.myopty.contracts.catalog.StockReservation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The order's half of a sale, without a database: what a reservation takes off
 * the shelf and audits, that a scarce shelf refuses it, and that a release puts
 * the units back with a {@code RETURN} row.
 *
 * <p>The repositories are hand-written fakes because what is under test is which
 * rows the service moves and when it refuses to move them. A fictional shortage
 * comes from a shelf with fewer units than the order asks for.
 *
 * <p>The low-stock alert rides along: the fake inventory reads each item's live
 * (post-move) level and threshold, and a recording sender captures what would
 * have been emailed, so these tests also pin down which moves raise an alert.
 */
class StockServiceTest {

    private final FakeFrameRepository frames = new FakeFrameRepository();
    private final FakeLensRepository lenses = new FakeLensRepository();
    private final FakeStockEntryRepository entries = new FakeStockEntryRepository();
    private final FakeInventory inventory = new FakeInventory();
    private final RecordingAlertSender alertSender = new RecordingAlertSender();
    private final StockService service =
            new StockService(frames, lenses, entries, new LowStockAlerts(inventory, alertSender, alertProperties()));

    @Test
    void reservingAnOrderTakesItsFrameAndLensOffTheShelf() {
        frames.store(1L, 12);
        lenses.store(2L, 7);

        service.reserve(new StockReservation(10L, "ORD-100", 1L, 2L, 2));

        assertThat(frames.stockOf(1L)).isEqualTo(10);
        assertThat(lenses.stockOf(2L)).isEqualTo(5);
        assertThat(entries.byType(StockItemType.FRAME)).singleElement().satisfies(entry -> {
            assertThat(entry.getChangeType()).isEqualTo(StockChangeType.SALE);
            assertThat(entry.getQuantityChange()).isEqualTo(-2);
            assertThat(entry.getStockAfter()).isEqualTo(10);
            assertThat(entry.getReference()).isEqualTo("ORD-100");
        });
        assertThat(entries.byType(StockItemType.LENS))
                .singleElement()
                .satisfies(entry -> assertThat(entry.getStockAfter()).isEqualTo(5));
    }

    @Test
    void aLensesOnlyOrderLeavesTheFrameShelfUntouched() {
        frames.store(1L, 12);
        lenses.store(2L, 7);

        service.reserve(new StockReservation(10L, "ORD-101", null, 2L, 1));

        assertThat(frames.stockOf(1L)).isEqualTo(12);
        assertThat(lenses.stockOf(2L)).isEqualTo(6);
        assertThat(entries.saved).extracting(StockEntry::getItemType).containsExactly(StockItemType.LENS);
    }

    @Test
    void reservingMoreThanAShelfHoldsIsRefused() {
        lenses.store(2L, 2);

        assertThatThrownBy(() -> service.reserve(new StockReservation(10L, "ORD-102", null, 2L, 5)))
                .isInstanceOf(InsufficientStockException.class)
                .satisfies(exception -> {
                    InsufficientStockException stockException = (InsufficientStockException) exception;
                    assertThat(stockException.getItemType()).isEqualTo("LENS");
                    assertThat(stockException.getItemId()).isEqualTo(2L);
                });
        assertThat(lenses.stockOf(2L)).isEqualTo(2);
        assertThat(entries.saved).isEmpty();
    }

    @Test
    void releasingARefusedOrderPutsItsUnitsBackWithAReturnEntry() {
        frames.store(1L, 10);
        lenses.store(2L, 5);

        service.release(new StockReservation(10L, "ORD-100", 1L, 2L, 2));

        assertThat(frames.stockOf(1L)).isEqualTo(12);
        assertThat(lenses.stockOf(2L)).isEqualTo(7);
        assertThat(entries.saved)
                .allSatisfy(entry -> assertThat(entry.getChangeType()).isEqualTo(StockChangeType.RETURN))
                .filteredOn(entry -> entry.getItemType() == StockItemType.FRAME)
                .singleElement()
                .satisfies(entry -> assertThat(entry.getQuantityChange()).isEqualTo(2));
    }

    @Test
    void aSaleThatCrossesTheThresholdAlertsTheShopWithTheNewLevel() {
        lenses.store(2L, 6);
        inventory.lens(2L, "Essential 1.50", 5);

        service.reserve(new StockReservation(10L, "ORD-200", null, 2L, 2));

        assertThat(lenses.stockOf(2L)).isEqualTo(4);
        assertThat(alertSender.recipients).containsExactly("shop@myopty.local");
        assertThat(alertSender.items).singleElement().satisfies(item -> {
            assertThat(item.itemType()).isEqualTo("LENS");
            assertThat(item.name()).isEqualTo("Essential 1.50");
            assertThat(item.stockQty()).isEqualTo(4);
            assertThat(item.status()).isEqualTo(StockStatus.LOW_STOCK);
        });
    }

    @Test
    void aSaleThatFillsTheShelfEmptyAlertsWithOutOfStock() {
        lenses.store(2L, 6);
        inventory.lens(2L, "Essential 1.50", 5);

        service.reserve(new StockReservation(10L, "ORD-201", null, 2L, 6));

        assertThat(alertSender.items).singleElement().satisfies(item -> {
            assertThat(item.stockQty()).isZero();
            assertThat(item.status()).isEqualTo(StockStatus.OUT_OF_STOCK);
        });
    }

    @Test
    void aSaleThatStaysInStockSendsNoAlert() {
        lenses.store(2L, 30);
        inventory.lens(2L, "Essential 1.50", 5);

        service.reserve(new StockReservation(10L, "ORD-202", null, 2L, 2));

        assertThat(alertSender.recipients).isEmpty();
    }

    @Test
    void aSaleOfAnItemAlreadyLowSendsNoAlert() {
        lenses.store(2L, 4);
        inventory.lens(2L, "Essential 1.50", 5);

        service.reserve(new StockReservation(10L, "ORD-203", null, 2L, 1));

        assertThat(alertSender.recipients).isEmpty();
    }

    private static LowStockAlertProperties alertProperties() {
        LowStockAlertProperties properties = new LowStockAlertProperties();
        properties.setRecipientEmail("shop@myopty.local");
        return properties;
    }

    private static final class FakeFrameRepository implements FrameRepository {

        private final Map<Long, Frame> frames = new HashMap<>();

        Frame store(long id, int stock) {
            Frame frame = new Frame();
            frame.setId(id);
            frame.setStockQty(stock);
            frames.put(id, frame);
            return frame;
        }

        int stockOf(long id) {
            return frames.get(id).getStockQty();
        }

        @Override
        public Frame save(Frame frame) {
            frames.put(frame.getId(), frame);
            return frame;
        }

        @Override
        public Optional<Frame> findById(Long id) {
            return Optional.ofNullable(frames.get(id));
        }

        @Override
        public Optional<Frame> findByIdAndActiveTrue(Long id) {
            return findById(id).filter(Frame::isActive);
        }

        @Override
        public List<Frame> findAllByOrderByIdAsc() {
            return frames.values().stream()
                    .sorted(Comparator.comparing(Frame::getId))
                    .toList();
        }

        @Override
        public List<Frame> findAllByActiveTrueOrderByIdAsc() {
            return frames.values().stream()
                    .filter(Frame::isActive)
                    .sorted(Comparator.comparing(Frame::getId))
                    .toList();
        }

        @Override
        public List<Frame> findActiveForStorefront(
                String categorySlug,
                java.math.BigDecimal minPrice,
                java.math.BigDecimal maxPrice,
                String color,
                String material,
                String q) {
            return frames.values().stream()
                    .filter(Frame::isActive)
                    .filter(frame -> {
                        if (minPrice != null
                                && frame.getPrice() != null
                                && frame.getPrice().compareTo(minPrice) < 0) {
                            return false;
                        }
                        if (maxPrice != null
                                && frame.getPrice() != null
                                && frame.getPrice().compareTo(maxPrice) > 0) {
                            return false;
                        }
                        if (color != null && !color.isBlank()) {
                            String c = frame.getColor();
                            if (c == null || !c.toLowerCase().contains(color.toLowerCase())) {
                                return false;
                            }
                        }
                        if (material != null && !material.isBlank()) {
                            String m = frame.getMaterial();
                            if (m == null || !m.toLowerCase().contains(material.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(Frame::getId))
                    .toList();
        }

        @Override
        public int decrementStock(long id, int quantity) {
            Frame frame = frames.get(id);
            if (frame == null || frame.getStockQty() < quantity) {
                return 0;
            }
            frame.setStockQty(frame.getStockQty() - quantity);
            return 1;
        }

        @Override
        public int incrementStock(long id, int quantity) {
            Frame frame = frames.get(id);
            if (frame == null) {
                return 0;
            }
            frame.setStockQty(frame.getStockQty() + quantity);
            return 1;
        }
    }

    private static final class FakeLensRepository implements LensRepository {

        private final Map<Long, Lens> lenses = new HashMap<>();

        Lens store(long id, int stock) {
            Lens lens = new Lens();
            lens.setId(id);
            lens.setStockQty(stock);
            lenses.put(id, lens);
            return lens;
        }

        int stockOf(long id) {
            return lenses.get(id).getStockQty();
        }

        @Override
        public Lens save(Lens lens) {
            lenses.put(lens.getId(), lens);
            return lens;
        }

        @Override
        public Optional<Lens> findById(Long id) {
            return Optional.ofNullable(lenses.get(id));
        }

        @Override
        public List<Lens> findAllByOrderByIdAsc() {
            return lenses.values().stream()
                    .sorted(Comparator.comparing(Lens::getId))
                    .toList();
        }

        @Override
        public int decrementStock(long id, int quantity) {
            Lens lens = lenses.get(id);
            if (lens == null || lens.getStockQty() < quantity) {
                return 0;
            }
            lens.setStockQty(lens.getStockQty() - quantity);
            return 1;
        }

        @Override
        public int incrementStock(long id, int quantity) {
            Lens lens = lenses.get(id);
            if (lens == null) {
                return 0;
            }
            lens.setStockQty(lens.getStockQty() + quantity);
            return 1;
        }

        @Override
        public List<Lens> findAllByActiveTrueOrderByIdAsc() {
            return lenses.values().stream()
                    .filter(Lens::isActive)
                    .sorted(Comparator.comparing(Lens::getId))
                    .toList();
        }
    }

    private static final class FakeStockEntryRepository implements StockEntryRepository {

        private final List<StockEntry> saved = new ArrayList<>();

        List<StockEntry> byType(StockItemType itemType) {
            return saved.stream()
                    .filter(entry -> entry.getItemType() == itemType)
                    .toList();
        }

        @Override
        public StockEntry save(StockEntry entry) {
            entry.setId((long) saved.size() + 1);
            saved.add(entry);
            return entry;
        }
    }

    /**
     * The alert's read of the catalog. It returns a row for the ids a test has
     * configured a threshold for, and reads the live count from the same fake
     * shelves the moves act on — so the row the alert classifies is the level
     * after the move, exactly as the real {@code JdbcTemplate} read would see
     * inside the transaction.
     */
    private final class FakeInventory extends InventoryReportRepository {

        private final Map<Long, String> frameNames = new HashMap<>();
        private final Map<Long, Integer> frameThresholds = new HashMap<>();
        private final Map<Long, String> lensNames = new HashMap<>();
        private final Map<Long, Integer> lensThresholds = new HashMap<>();

        FakeInventory() {
            super(null);
        }

        void frame(long id, String name, int threshold) {
            frameNames.put(id, name);
            frameThresholds.put(id, threshold);
        }

        void lens(long id, String name, int threshold) {
            lensNames.put(id, name);
            lensThresholds.put(id, threshold);
        }

        @Override
        public Optional<StockRow> activeFrame(long id) {
            Integer threshold = frameThresholds.get(id);
            return threshold == null
                    ? Optional.empty()
                    : Optional.of(new StockRow("FRAME", id, frameNames.get(id), frames.stockOf(id), threshold));
        }

        @Override
        public Optional<StockRow> activeLens(long id) {
            Integer threshold = lensThresholds.get(id);
            return threshold == null
                    ? Optional.empty()
                    : Optional.of(new StockRow("LENS", id, lensNames.get(id), lenses.stockOf(id), threshold));
        }
    }

    /** Captures what would have been emailed, so the tests can assert on it. */
    private static final class RecordingAlertSender implements LowStockAlertSender {

        private final List<String> recipients = new ArrayList<>();
        private final List<AvailabilityItemResponse> items = new ArrayList<>();

        @Override
        public void send(List<AvailabilityItemResponse> items, String recipientEmail) {
            recipients.add(recipientEmail);
            this.items.addAll(items);
        }
    }
}
