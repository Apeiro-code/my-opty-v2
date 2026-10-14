package com.myopty.billing.exception;

/**
 * A resource named in a request does not exist, or does not belong to the caller.
 *
 * <p>The two cases answer the same 404 on purpose: a customer asking for another
 * customer's payment must not be able to tell "not yours" from "not there", or the
 * difference becomes a way to probe for other people's records.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
