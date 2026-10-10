package com.myopty.order.exception;

/**
 * A resource the caller asked for does not exist, or does not belong to them.
 *
 * <p>The two cases share one exception on purpose. Distinguishing "no such
 * prescription" from "someone else's prescription" in the response would tell a
 * customer that another customer's id exists, so both answer 404 with the same
 * message.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
