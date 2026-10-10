package com.myopty.catalog.exception;

/**
 * A frame the caller asked for does not exist.
 *
 * <p>The catalog keeps every frame, active or not, so there is one "not found"
 * rather than a separate "not found but maybe hidden" — an id that is absent is
 * absent, and the message never says whether it once existed.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
