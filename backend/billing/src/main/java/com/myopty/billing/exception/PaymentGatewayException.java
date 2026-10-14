package com.myopty.billing.exception;

/**
 * The gateway could not be reached, or answered in a way the adapter could not use,
 * after billing had already decided to charge or refund.
 *
 * <p>Answered 502: the fault is downstream, the request was fine, and the payment
 * row records what was attempted so the state is not lost.
 */
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }
}
