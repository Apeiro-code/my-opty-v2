package com.myopty.contracts.order;

import java.util.Optional;

/**
 * The discount seam: what {@code catalog} asks {@code order} when it prices a
 * product for the storefront, so a customer sees the discounted figure next to
 * the original without catalog reading order's tables.
 *
 * <p>Implemented by {@code order}'s {@code DiscountQueryImpl}. It answers with
 * one promotion — the best a single item can get — rather than a list, so the
 * rule for which promotion wins lives with the discounts, not spread across every
 * consumer. No promotion covers the item means {@link Optional#empty()}.
 */
public interface DiscountQuery {

    /**
     * The single best promotion for one item, or empty if it is full price.
     *
     * @param kind which product the id names
     * @param itemId the frame or lens id
     */
    Optional<AppliedDiscount> bestDiscountFor(ItemKind kind, long itemId);
}
