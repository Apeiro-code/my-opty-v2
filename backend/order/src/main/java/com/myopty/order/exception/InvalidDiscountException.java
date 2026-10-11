package com.myopty.order.exception;

/**
 * A promotion request that is internally inconsistent — currently a validity
 * window that ends before it starts.
 *
 * <p>Separate from {@link InvalidStateException} because the two answer different
 * questions: this is "the shape you sent is wrong" (400), that is "the row has
 * already moved past this decision" (409). Bean Validation catches the
 * single-field cases before the service is entered; this carries the rule that
 * spans two fields.
 */
public class InvalidDiscountException extends RuntimeException {

    public InvalidDiscountException(String message) {
        super(message);
    }
}
