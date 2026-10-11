package com.myopty.order.service;

import com.myopty.contracts.order.AppliedDiscount;
import com.myopty.contracts.order.DiscountQuery;
import com.myopty.contracts.order.ItemKind;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * The order module's side of {@link DiscountQuery}: it exposes the discount rule
 * to catalog without catalog touching {@code discount}. It is a thin adapter so
 * the rule itself stays in {@link DiscountService} with the rest of the
 * promotion logic and is unit-tested there.
 */
@Service
public class DiscountQueryImpl implements DiscountQuery {

    private final DiscountService discounts;

    public DiscountQueryImpl(DiscountService discounts) {
        this.discounts = discounts;
    }

    @Override
    public Optional<AppliedDiscount> bestDiscountFor(ItemKind kind, long itemId) {
        return discounts.bestDiscountFor(kind, itemId);
    }
}
