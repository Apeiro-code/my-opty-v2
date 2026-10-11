package com.myopty.contracts.catalog;

/**
 * The catalog's stock, as the order module needs it: take a placed order's units
 * off the shelf, and put them back if the order is refused.
 *
 * <p>The implementation lives in {@code catalog} and owns every table it touches;
 * the order module only sees this interface, so the two modules stay independent.
 * {@link #reserve(StockReservation)} is expected to run inside the caller's
 * transaction, so an order that cannot be filled rolls back whole.
 *
 * <p>The interface returns nothing: what the order module needs to know is
 * whether the reservation held, and a refused one is reported as an
 * {@link InsufficientStockException} rather than a return value that a caller
 * could ignore.
 */
public interface StockManager {

    /** Takes the reservation's units off the shelf, or throws if there are too few. */
    void reserve(StockReservation reservation);

    /** Puts a previously reserved order's units back, for a refused order. */
    void release(StockReservation reservation);
}
