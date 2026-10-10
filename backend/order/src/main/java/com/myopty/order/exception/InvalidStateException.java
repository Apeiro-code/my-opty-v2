package com.myopty.order.exception;

/**
 * A review or approval step was asked for at a point the workflow does not allow.
 *
 * <p>Reviewing is one-way (a prescription already decided cannot be re-decided) and
 * an order can only be approved or rejected while it is {@code PENDING}. Those are
 * durable rules, not client mistakes in the shape of the request, so the caller is
 * answered 409 rather than 400.
 */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
