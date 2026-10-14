package com.myopty.billing.exception;

/**
 * A refund was asked for against a payment that cannot take one: it never settled,
 * or it has already been refunded in full.
 *
 * <p>A durable state conflict — 409 — rather than a bad request, because the
 * request was well-formed and the payment's own history is what refuses it.
 */
public class RefundNotAllowedException extends RuntimeException {

    public RefundNotAllowedException(String message) {
        super(message);
    }
}
