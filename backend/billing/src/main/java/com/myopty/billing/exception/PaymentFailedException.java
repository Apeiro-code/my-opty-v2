package com.myopty.billing.exception;

/**
 * The gateway refused the charge: a declined card, a failed verification, or a
 * method the provider will not settle.
 *
 * <p>It carries the provider's reason because it is what the customer is shown,
 * and answers 402 — "the payment is the problem" — distinct from a 400 (bad
 * request shape) and a 500 (the server failed).
 */
public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }
}
