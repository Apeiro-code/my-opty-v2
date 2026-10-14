package com.myopty.billing.exception;

/**
 * An invoice could not be rendered as a PDF.
 *
 * <p>Wraps the PDF library's checked exception so no caller outside the renderer
 * has to know which library renders it, and answers 500 because it is a failure of
 * the server, not the request.
 */
public class InvoiceRenderingException extends RuntimeException {

    public InvoiceRenderingException(String message, Throwable cause) {
        super(message, cause);
    }
}
