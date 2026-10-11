package com.myopty.catalog.model;

/**
 * What a browsing category may hold, mirroring {@code category.item_type}'s
 * {@code chk_category_item_type} constraint (V2).
 *
 * <p>{@code BOTH} is the value that lets one category serve frames and lenses,
 * so the {@link #allows} check is a property of the type rather than a condition
 * each service repeats.
 */
public enum CategoryItemType {
    FRAME,
    LENS,
    BOTH;

    /** Whether a category of this type may hold a record of the given type. */
    public boolean allows(CategoryItemType expected) {
        return this == BOTH || this == expected;
    }
}
