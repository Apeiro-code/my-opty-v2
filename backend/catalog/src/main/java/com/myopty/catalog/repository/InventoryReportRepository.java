package com.myopty.catalog.repository;

import java.util.List;
import java.util.Optional;
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

    /*
     * The single-item reads back the low-stock alert: after a sale moves an item,
     * the alert needs that item's name and threshold to decide whether the move
     * crossed into a band the shop must act on. They are scoped to active items
     * for the same reason the report is — a discontinued item is not something the
     * shop reorders, so a sale of one raises no alert.
     */
    private static final String FRAME_BY_ID =
            "SELECT id, model AS name, stock_qty, low_stock_threshold FROM frame WHERE id = ? AND is_active = TRUE";

    private static final String LENS_BY_ID =
            "SELECT id, name, stock_qty, low_stock_threshold FROM lens WHERE id = ? AND is_active = TRUE";

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

    /** One active frame's live stock and threshold, or empty if it is gone or discontinued. */
    public Optional<StockRow> activeFrame(long id) {
        return jdbc
                .query(
                        FRAME_BY_ID,
                        (rs, rowNum) -> new StockRow(
                                "FRAME",
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getInt("stock_qty"),
                                rs.getInt("low_stock_threshold")),
                        id)
                .stream()
                .findFirst();
    }

    /** One active lens's live stock and threshold, or empty if it is gone or discontinued. */
    public Optional<StockRow> activeLens(long id) {
        return jdbc
                .query(
                        LENS_BY_ID,
                        (rs, rowNum) -> new StockRow(
                                "LENS",
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getInt("stock_qty"),
                                rs.getInt("low_stock_threshold")),
                        id)
                .stream()
                .findFirst();
    }
}
