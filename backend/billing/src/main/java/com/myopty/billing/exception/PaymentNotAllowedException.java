package com.myopty.billing.exception;

/**
 * The payment is refused by billing's own rules rather than the gateway: the order
 * has already been paid, or it is in a state (rejected) that must not be charged.
 *
 * <p>A durable state conflict, so 409, and distinct from {@link InvalidPaymentException},
 * which is about the shape of the request.
 */
public class PaymentNotAllowedException extends RuntimeException {

    public PaymentNotAllowedException(String message) {
        super(message);
    }
}
