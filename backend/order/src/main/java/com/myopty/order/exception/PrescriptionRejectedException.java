package com.myopty.order.exception;

/**
 * An order was placed against a prescription the shop has rejected.
 *
 * <p>A rejection is a durable decision, so a rejected prescription must not be
 * linked to a new order. Approving an order against a prescription that is still
 * awaiting review is fine — that gate is a later story — so only the rejected
 * value is refused here.
 */
public class PrescriptionRejectedException extends RuntimeException {

    public PrescriptionRejectedException(String message) {
        super(message);
    }
}
