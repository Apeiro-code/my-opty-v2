package com.myopty.order.dto;

import com.myopty.order.model.Discount;
import com.myopty.order.model.DiscountState;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A promotion read back for the shop: what it offers, what it targets, how long
 * it runs, and where it currently sits in its life.
 *
 * <p>{@code targetItemIds} is the decoded JSON list, or null for a sitewide
 * promotion; {@code sitewide} states the same thing as a boolean so the frontend
 * does not have to treat null and empty as the same case. {@code state} is
 * computed at read time from the flag and the dates, so a list call always
 * reports the truth as of now without a scheduled job to sweep rows.
 */
public record DiscountResponse(
        long id,
        String description,
        BigDecimal percentage,
        List<Long> targetItemIds,
        boolean sitewide,
        LocalDate validFrom,
        LocalDate validTo,
        boolean active,
        DiscountState state,
        LocalDateTime endedAt,
        Long createdBy) {

    public static DiscountResponse from(Discount discount, List<Long> targetItemIds, DiscountState state) {
        return new DiscountResponse(
                discount.getId(),
                discount.getDescription(),
                discount.getPercentage(),
                targetItemIds,
                targetItemIds == null || targetItemIds.isEmpty(),
                discount.getValidFrom(),
                discount.getValidTo(),
                discount.isActive(),
                state,
                discount.getEndedAt(),
                discount.getCreatedBy());
    }
}
