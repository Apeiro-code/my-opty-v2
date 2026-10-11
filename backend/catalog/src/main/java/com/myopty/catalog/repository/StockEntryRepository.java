package com.myopty.catalog.repository;

import com.myopty.catalog.model.StockEntry;
import org.springframework.data.repository.Repository;

/**
 * Writes {@code stock_entry} rows.
 *
 * <p>A narrow marker repository: this story appends audit rows when an order
 * takes or returns stock. Reading the log back is a reporting story that will
 * widen it deliberately.
 */
public interface StockEntryRepository extends Repository<StockEntry, Long> {

    StockEntry save(StockEntry entry);
}
