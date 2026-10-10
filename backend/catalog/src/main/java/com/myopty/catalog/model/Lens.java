package com.myopty.catalog.model;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A lens the shop sells: its name, the correction it provides, any coating, its
 * price and how many are on the shelf.
 *
 * <p>Only the columns the add story owns are mapped. {@code low_stock_threshold}
 * and {@code description} exist in V4 and are left unmapped here, so their column
 * defaults survive a save and this story cannot quietly overwrite a value another
 * story sets. The database fills {@code created_at} and {@code updated_at} from
 * their column defaults, so no fields exist for them.
 */
@Table("lens")
public class Lens {

    @Id
    private Long id;

    private Long categoryId;
    private String name;
    private LensType type;
    private String coating;
    private BigDecimal price;
    private int stockQty;

    /** Named explicitly: the property is {@code active} but the column is {@code is_active}. */
    @Column("is_active")
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LensType getType() {
        return type;
    }

    public void setType(LensType type) {
        this.type = type;
    }

    public String getCoating() {
        return coating;
    }

    public void setCoating(String coating) {
        this.coating = coating;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
