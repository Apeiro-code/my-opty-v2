package com.myopty.catalog.exception;

/**
 * The availability report could not be rendered as a file.
 *
 * <p>A client asking for PDF or Excel made a valid request; the failure is the
 * server's ability to produce the document, so it maps to a 500 rather than a
 * 400. Wrapping the library's checked exception in an unchecked one keeps the
 * controller free of format-specific error handling.
 */
public class ReportExportException extends RuntimeException {

    public ReportExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
