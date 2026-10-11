package com.myopty.contracts.catalog;

/**
 * A reservation asked for more units of a frame or lens than are on the shelf.
 *
 * <p>It is a plain runtime exception with no Spring and no entity behind it, so
 * the order module can catch it at its own boundary and turn it into its own
 * error envelope without knowing anything else about the catalog.
 */
public class InsufficientStockException extends RuntimeException {

    private final String itemType;
    private final long itemId;

    public InsufficientStockException(String itemType, long itemId) {
        super("There are not enough " + itemType.toLowerCase() + " units in stock to fill this order.");
        this.itemType = itemType;
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public long getItemId() {
        return itemId;
    }
}
