package com.myopty.order.model;

/**
 * Where an order sits in the shop's workflow.
 *
 * <p>Mirrors the {@code chk_progressive_order_status} constraint on
 * {@code progressive_order} (V101). The transitions between these values are a
 * later story; this story only ever writes {@link #PENDING}.
 */
public enum OrderStatus {
    PENDING,
    APPROVED,
    PROCESSING,
    READY,
    DISPATCHED,
    REJECTED
}
