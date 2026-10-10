package com.myopty.order.exception;

/**
 * An order was approved while its prescription had not been verified.
 *
 * <p>This is the gate that keeps unreviewed work out of production: the shop may
 * only approve an order whose prescription is {@code VERIFIED}. It is a conflict
 * with the current state of the linked prescription, so the caller is answered
 * 409 rather than 400.
 */
public class PrescriptionNotVerifiedException extends RuntimeException {

    public PrescriptionNotVerifiedException(String message) {
        super(message);
    }
}
