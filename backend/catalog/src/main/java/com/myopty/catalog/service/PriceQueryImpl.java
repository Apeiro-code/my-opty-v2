package com.myopty.catalog.service;

import com.myopty.catalog.model.Frame;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.repository.FrameRepository;
import com.myopty.catalog.repository.LensRepository;
import com.myopty.contracts.catalog.PriceQuery;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * The catalog's side of {@link PriceQuery}: it exposes a product's shelf price to
 * billing without billing reading {@code frame} or {@code lens}.
 *
 * <p>It reads the price through the same repositories the rest of the module uses,
 * so there is one definition of "what a product costs". The lookup is by id alone,
 * not gated on {@code is_active}: an order placed before a product was discontinued
 * must still price, and the storefront's active-only filter belongs to the browser,
 * not to settling an order.
 *
 * <p>A missing product answers empty rather than a price of zero, so billing can
 * tell "this frame no longer exists" apart from "this frame is free" and refuse the
 * payment instead of charging nothing.
 */
@Service
public class PriceQueryImpl implements PriceQuery {

    private final FrameRepository frames;
    private final LensRepository lenses;

    public PriceQueryImpl(FrameRepository frames, LensRepository lenses) {
        this.frames = frames;
        this.lenses = lenses;
    }

    @Override
    public Optional<BigDecimal> framePrice(long frameId) {
        return frames.findById(frameId).map(Frame::getPrice);
    }

    @Override
    public Optional<BigDecimal> lensPrice(long lensId) {
        return lenses.findById(lensId).map(Lens::getPrice);
    }
}
