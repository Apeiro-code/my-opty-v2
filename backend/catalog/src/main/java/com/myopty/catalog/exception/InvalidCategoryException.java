package com.myopty.catalog.exception;

/**
 * A frame or lens was filed under a category it cannot belong to: an id that does
 * not exist, or one whose {@code item_type} is for the other product.
 *
 * <p>Both are client mistakes rather than server ones, so they answer 400. The
 * database would refuse the absent id through the foreign key, but catching it
 * here names the value that was wrong instead of the generic constraint failure.
 */
public class InvalidCategoryException extends RuntimeException {

    public InvalidCategoryException(String message) {
        super(message);
    }
}
