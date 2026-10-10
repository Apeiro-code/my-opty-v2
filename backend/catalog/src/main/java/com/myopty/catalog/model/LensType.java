package com.myopty.catalog.model;

/**
 * The kind of correction a lens provides, and the value the order module copies
 * onto an order as its routing key.
 *
 * <p>The values mirror {@code lens.type}'s {@code chk_lens_type} constraint (V4)
 * and order's {@code OrderType}. They are duplicated rather than shared because
 * catalog may not depend on order, and order may not read the catalog's tables;
 * putting the enum in {@code contracts} is the change that would make one copy
 * shared, and that is a team decision.
 */
public enum LensType {
    SINGLE_VISION,
    BIFOCAL,
    PROGRESSIVE
}
