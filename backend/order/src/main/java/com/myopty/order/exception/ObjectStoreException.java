package com.myopty.order.exception;

/**
 * The object store could not be read or written. Runtime because callers cannot
 * repair a full disk or a refused path in the request thread; the controller
 * advice turns it into a 500 with the request's error envelope.
 */
public class ObjectStoreException extends RuntimeException {

    public ObjectStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
