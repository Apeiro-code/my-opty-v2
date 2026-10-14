package com.myopty.contracts.order;

import java.util.Optional;

/**
 * The order seam: what {@code billing} asks {@code order} when it needs to pay
 * for an order, so billing never reads {@code progressive_order}.
 *
 * <p>It answers with one {@link OrderPaymentView} rather than the whole order,
 * because a payment only needs the owner, the reference, the products to price
 * and the workflow state. A query by id, not a list, because a payment is always
 * for one known order. Implemented by {@code order}'s {@code OrderQueryImpl}.
 */
public interface OrderQuery {

    /** The order as a payment sees it, or empty when no such order exists. */
    Optional<OrderPaymentView> findForPayment(long orderId);
}
