package com.myopty.order.model;

/**
 * Where an order sits in the shop's workflow.
 *
 * <p>Mirrors the {@code chk_progressive_order_status} constraint on
 * {@code progressive_order} (V101). The production line runs
 * {@link #PENDING} → {@link #APPROVED} → {@link #PROCESSING} → {@link #READY} →
 * {@link #DISPATCHED}; a step may be skipped because a frame already in stock can
 * go straight to ready, but the order never moves backwards. {@link #REJECTED} is
 * a branch off {@code PENDING}, not a later stage, which is why it does not fit
 * ordinals and gets its own rank below.
 */
public enum OrderStatus {
    PENDING,
    APPROVED,
    PROCESSING,
    READY,
    DISPATCHED,
    REJECTED;

    /**
     * The position on the production line, or {@code -1} for the branch that is
     * not on it. {@link #REJECTED} sits last in the enum so an ordinal would
     * otherwise read as "further along than dispatched"; this is the ordering the
     * transition rule actually means.
     */
    public int progressRank() {
        return this == REJECTED ? -1 : ordinal();
    }

    /** Whether an order at {@code current} may move to {@code target}. */
    public static boolean isForwardTransition(OrderStatus current, OrderStatus target) {
        return current.progressRank() >= APPROVED.progressRank()
                && target.progressRank() > current.progressRank()
                && target != REJECTED;
    }
}
