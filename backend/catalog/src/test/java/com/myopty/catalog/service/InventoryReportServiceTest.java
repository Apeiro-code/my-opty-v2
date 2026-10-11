package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.dto.AvailabilityReportResponse;
import com.myopty.catalog.model.StockStatus;
import com.myopty.catalog.repository.InventoryReportRepository;
import com.myopty.catalog.repository.StockRow;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The report's classification rules, without a database: which shelf counts land
 * in which band and how the three bands are counted.
 *
 * <p>{@link InventoryReportRepository} is a hand-written stub because what is
 * under test is the banding and the sums, not the SQL that feeds them.
 */
class InventoryReportServiceTest {

    private final InventoryReportService service = new InventoryReportService(reporting(
            List.of(
                    new StockRow("FRAME", 1L, "Astra 2100", 12, 5),
                    new StockRow("FRAME", 2L, "Astra 2100 Tortoise", 3, 5),
                    new StockRow("FRAME", 3L, "Discontinued", 0, 5)),
            List.of(new StockRow("LENS", 10L, "Essential 1.50", 30, 5))));

    @Test
    void stockAboveTheThresholdIsInStock() {
        AvailabilityItemResponse item = service.generate().items().get(0);

        assertThat(item.status()).isEqualTo(StockStatus.IN_STOCK);
    }

    @Test
    void stockAtOrBelowTheThresholdIsLow() {
        AvailabilityItemResponse item = service.generate().items().get(1);

        assertThat(item.status()).isEqualTo(StockStatus.LOW_STOCK);
    }

    @Test
    void emptyStockIsOutOfStock() {
        AvailabilityItemResponse item = service.generate().items().get(2);

        assertThat(item.status()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void theReportCountsItemsAndSumsEachBand() {
        AvailabilityReportResponse report = service.generate();

        assertThat(report.totalItems()).isEqualTo(4);
        assertThat(report.inStockCount()).isEqualTo(2);
        assertThat(report.lowStockCount()).isEqualTo(1);
        assertThat(report.outOfStockCount()).isEqualTo(1);
    }

    @Test
    void theAlertsAreTheItemsNotInStockOutOfStockFirst() {
        List<AvailabilityItemResponse> alerts = service.alerts();

        assertThat(alerts)
                .extracting(AvailabilityItemResponse::name)
                .containsExactly("Discontinued", "Astra 2100 Tortoise");
        assertThat(alerts)
                .extracting(AvailabilityItemResponse::status)
                .containsExactly(StockStatus.OUT_OF_STOCK, StockStatus.LOW_STOCK);
    }

    private static InventoryReportRepository reporting(List<StockRow> frames, List<StockRow> lenses) {
        return new InventoryReportRepository(null) {
            @Override
            public List<StockRow> activeFrames() {
                return frames;
            }

            @Override
            public List<StockRow> activeLenses() {
                return lenses;
            }
        };
    }
}
