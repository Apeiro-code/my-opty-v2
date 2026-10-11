package com.myopty.order.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A promotion: a percentage off a set of products for a window of dates.
 *
 * <p>{@code targetItemIds} is the raw JSON the V103 column stores — a flat array
 * of frame and lens ids, or null for a sitewide promotion. It is held as a
 * {@code String} rather than a mapped collection because MySQL's JSON type has no
 * natural Spring Data JDBC mapping; the service encodes and decodes it through the
 * application's Jackson {@code ObjectMapper}. The database already documents the
 * cost of this shape: an id carries no type, so a frame id and a lens id that
 * happen to match are indistinguishable (see V103).
 *
 * <p>{@code active} is the mapping of the table's {@code is_active}: the shop's
 * flag for whether the promotion runs at all, kept separate from the dates so
 * "end it early" is a decision rather than a date edit. {@code created_at} and
 * {@code updated_at} are left unmapped so their column defaults survive a save.
 */
@Table("discount")
public class Discount {

    @Id
    private Long id;

    private String description;
    private BigDecimal percentage;
    private String targetItemIds;
    private LocalDate validFrom;
    private LocalDate validTo;

    @Column("is_active")
    private boolean active;

    private LocalDateTime endedAt;
    private Long createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getTargetItemIds() {
        return targetItemIds;
    }

    public void setTargetItemIds(String targetItemIds) {
        this.targetItemIds = targetItemIds;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
