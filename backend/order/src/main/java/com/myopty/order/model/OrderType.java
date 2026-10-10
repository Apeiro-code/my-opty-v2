package com.myopty.order.model;

/**
 * The lens type an order was placed for, and the key the workflow routes on.
 *
 * <p>The values mirror {@code lens.type} in the catalog (V4) and the
 * {@code chk_progressive_order_type} constraint (V107). They are copied onto the
 * order at creation rather than looked up later, so routing an order never reads
 * the catalog module's tables.
 */
public enum OrderType {
    SINGLE_VISION,
    BIFOCAL,
    PROGRESSIVE
}
