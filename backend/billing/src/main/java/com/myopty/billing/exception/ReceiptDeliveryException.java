package com.myopty.billing.exception;

/**
 * A receipt or refund confirmation could not be sent, raised only when
 * {@code myopty.billing.fail-on-error} is on.
 *
 * <p>The payment is already recorded — the failure is the email, after the fact —
 * so this answers 502 and never rolls back what happened.
 */
public class ReceiptDeliveryException extends RuntimeException {

    public ReceiptDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
