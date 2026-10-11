package com.myopty.catalog.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * The report's read of {@code frame} and {@code lens}.
 *
 * <p>A deliberately small {@link JdbcTemplate} reader rather than Spring Data
 * query methods: the report needs a projection across two tables that the
 * entities deliberately do not carry (their {@code low_stock_threshold} is
 * unmapped so a save cannot overwrite the column default). Two narrow SELECTs
 * against this module's own tables are inside its boundary and keep the code a
 * reader can follow instead of a mapping convention to remember.
 *
 * <p>Only active items are reported: a discontinued frame or lens is not
 * something the shop reorders, though its stock row is still on the shelf.
 */
@Repository
public class InventoryReportRepository {

    private static final String ACTIVE_FRAMES = "SELECT id, model AS name, stock_qty, low_stock_threshold"
            + " FROM frame WHERE is_active = TRUE ORDER BY id";

    private static final String ACTIVE_LENSES =
            "SELECT id, name, stock_qty, low_stock_threshold FROM lens WHERE is_active = TRUE ORDER BY id";

    private final JdbcTemplate jdbc;

    public InventoryReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<StockRow> activeFrames() {
        return jdbc.query(
                ACTIVE_FRAMES,
                (rs, rowNum) -> new StockRow(
                        "FRAME",
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("stock_qty"),
                        rs.getInt("low_stock_threshold")));
    }

    public List<StockRow> activeLenses() {
        return jdbc.query(
                ACTIVE_LENSES,
                (rs, rowNum) -> new StockRow(
                        "LENS",
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("stock_qty"),
                        rs.getInt("low_stock_threshold")));
    }
}
