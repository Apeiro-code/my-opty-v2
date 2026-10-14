package com.myopty.contracts.catalog;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * The pricing seam: what {@code billing} asks {@code catalog} for when it needs
 * the shelf price of one product, so a payment can be charged without billing
 * reading catalog's tables.
 *
 * <p>Two methods rather than one taking an {@code ItemKind}: a frame id and a
 * lens id live in separate sequences, so naming the kind in the method removes
 * the ambiguity of a single numeric id and keeps the contract free of another
 * module's enum. The answer is the current catalogue price, not a snapshot —
 * billing stores what it charged on the invoice so a later price change cannot
 * rewrite history.
 *
 * <p>Implemented by {@code catalog}'s {@code PriceQueryImpl}. An id that names
 * no product answers {@link Optional#empty()}, which the caller treats as "the
 * product is gone" rather than a price of zero.
 */
public interface PriceQuery {

    /** The current selling price of a frame, or empty when no such frame exists. */
    Optional<BigDecimal> framePrice(long frameId);

    /** The current selling price of a lens, or empty when no such lens exists. */
    Optional<BigDecimal> lensPrice(long lensId);
}
