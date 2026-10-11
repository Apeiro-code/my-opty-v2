package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.catalog.model.Frame;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.StockChangeType;
import com.myopty.catalog.model.StockEntry;
import com.myopty.catalog.model.StockItemType;
import com.myopty.catalog.repository.FrameRepository;
import com.myopty.catalog.repository.LensRepository;
import com.myopty.catalog.repository.StockEntryRepository;
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
 */
class StockServiceTest {

    private final FakeFrameRepository frames = new FakeFrameRepository();
    private final FakeLensRepository lenses = new FakeLensRepository();
    private final FakeStockEntryRepository entries = new FakeStockEntryRepository();
    private final StockService service = new StockService(frames, lenses, entries);

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
        public List<Frame> findAllByOrderByIdAsc() {
            return frames.values().stream()
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
}
