package com.myopty.order.exception;

/**
 * An uploaded document was refused before it was stored: an empty file, a type
 * outside the allowlist, or one larger than the configured cap.
 *
 * <p>A distinct type from {@link ObjectStoreException} because the two become
 * different HTTP answers: this one is the caller's to fix (400), while a store
 * failure is the server's (500). Collapsing them would tell a customer their
 * unsupported file was a server fault.
 */
public class InvalidDocumentException extends RuntimeException {

    public InvalidDocumentException(String message) {
        super(message);
    }
}
