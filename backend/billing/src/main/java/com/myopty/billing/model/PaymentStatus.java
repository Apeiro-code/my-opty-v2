package com.myopty.billing.model;

/**
 * Where a payment sits, mirroring the {@code chk_payment_status} constraint on
 * {@code payment} (V301, widened by V305).
 *
 * <p>The path is {@link #PENDING} → {@link #AUTHORIZED} → {@link #CAPTURED}, with
 * {@link #FAILED} as a branch off the first two when the gateway refuses.
 * {@link #PARTIALLY_REFUNDED} and {@link #REFUNDED} are where a captured payment
 * ends up once money goes back: the first when some is returned, the second when
 * all of it is. A payment never leaves {@code CAPTURED} without a refund, which is
 * what lets the refund service treat the two refund states as the only ones it may
 * act on.
 *
 * <p>The names are the column values; the database rejects anything else, so this
 * enum is the compiler-side mirror of a rule the schema already enforces.
 */
public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    CAPTURED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    FAILED;

    /** Whether the gateway has taken the money and it has not been returned in full. */
    public boolean isSettled() {
        return this == CAPTURED || this == PARTIALLY_REFUNDED;
    }

    /** Whether a refund may be issued against a payment in this state. */
    public boolean isRefundable() {
        return isSettled();
    }
}
