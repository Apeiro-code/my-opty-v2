package com.myopty.billing.exception;

/**
 * The payment request is malformed in a way the bean validator cannot express: an
 * unknown payment method, a refund amount that is not a positive value the payment
 * can cover, or a method that needs details that were not sent.
 *
 * <p>Distinct from {@link PaymentFailedException}: this is "you sent something the
 * server will not act on" (400), not "the gateway refused" (402).
 */
public class InvalidPaymentException extends RuntimeException {

    public InvalidPaymentException(String message) {
        super(message);
    }
}
