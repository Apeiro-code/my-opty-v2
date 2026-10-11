package com.myopty.catalog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One row of the stock movement audit log: what left or entered the shelf, why,
 * and what was left afterwards.
 *
 * <p>Only the columns this story writes are mapped. {@code created_at} is filled
 * by the column default, so there is no field for it; {@code created_by} is null
 * because an order's movement is the system acting, not a logged-in user. The V5
 * comment explains why {@code (item_type, item_id)} has no foreign key: one audit
 * table cannot have one constraint for two product tables.
 */
@Table("stock_entry")
public class StockEntry {

    @Id
    private Long id;

    private StockItemType itemType;
    private Long itemId;
    private StockChangeType changeType;
    private int quantityChange;
    private int stockAfter;
    private String reference;
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StockItemType getItemType() {
        return itemType;
    }

    public void setItemType(StockItemType itemType) {
        this.itemType = itemType;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public StockChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(StockChangeType changeType) {
        this.changeType = changeType;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public int getStockAfter() {
        return stockAfter;
    }

    public void setStockAfter(int stockAfter) {
        this.stockAfter = stockAfter;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
