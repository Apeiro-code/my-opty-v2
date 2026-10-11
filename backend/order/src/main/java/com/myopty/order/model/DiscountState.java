package com.myopty.order.model;

/**
 * Where a discount sits in its life, derived from its validity window and the
 * shop's decision rather than stored.
 *
 * <p>Deriving it keeps the four states consistent with the one row: {@code
 * is_active} says whether the shop has called the promotion off, and the dates
 * say whether it has started or run out. A stored state would be a fifth fact to
 * keep in step with the other two, and would drift the moment a date passed.
 */
public enum DiscountState {

    /** Running now: not ended, and today falls inside the window. */
    ACTIVE,

    /** Approved but not started: today is before {@code valid_from}. */
    SCHEDULED,

    /** The window closed while the discount was still flagged active. */
    EXPIRED,

    /** The shop ended it early, before {@code valid_to}. */
    ENDED
}
