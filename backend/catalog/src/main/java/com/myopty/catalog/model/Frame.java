package com.myopty.catalog.model;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A frame the shop sells: the model and its colourway, what it is made of, its
 * price and how many are on the shelf.
 *
 * <p>Only the columns the add/edit stories own are mapped. {@code shape},
 * {@code image_url}, {@code description} and {@code low_stock_threshold} exist in
 * V3 and are left unmapped here, so their column defaults survive a save and this
 * story cannot quietly overwrite a value another story sets. An unmapped field is
 * not a missing one; adding it to the form is the change that adds it here.
 *
 * <p>The database fills {@code created_at} and {@code updated_at} from their column
 * defaults, so no fields exist for them and a save never writes them.
 */
@Table("frame")
public class Frame {

    @Id
    private Long id;

    private Long categoryId;
    private String model;
    private String color;
    private String material;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
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
