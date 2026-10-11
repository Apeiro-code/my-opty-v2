package com.myopty.contracts.order;

/**
 * Which product an id refers to when asking {@link DiscountQuery} for a price.
 *
 * <p>The question has to name the kind because a frame id and a lens id live in
 * separate sequences and may collide. It is a local enum, not catalog's, so this
 * contract keeps its no-dependencies rule and neither module has to import the
 * other's types.
 */
public enum ItemKind {
    FRAME,
    LENS
}
