package com.myopty.catalog.service;

import com.myopty.catalog.dto.CreateFrameRequest;
import com.myopty.catalog.dto.FrameResponse;
import com.myopty.catalog.dto.UpdateFrameRequest;
import com.myopty.catalog.exception.ResourceNotFoundException;
import com.myopty.catalog.model.CategoryItemType;
import com.myopty.catalog.model.Frame;
import com.myopty.catalog.repository.FrameRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a frame to the shop's catalogue and keeps an existing one accurate.
 *
 * <p>A frame may be filed under a category or left unfiled: the V3 column is
 * nullable, so a null {@code categoryId} is a valid choice and only a non-null
 * one is checked against the category's {@code item_type}. The price and stock
 * rules are enforced by Bean Validation before the service is entered, so the
 * service only has to apply the defaults and persist.
 *
 * <p>An edit loads the row first so a missing id is a 404 rather than a silent
 * insert, then writes back the whole record: the endpoint is a PUT, so an omitted
 * field is a client mistake the request's constraints have already refused.
 */
@Service
public class FrameService {

    private final FrameRepository frames;
    private final CategoryService categories;

    public FrameService(FrameRepository frames, CategoryService categories) {
        this.frames = frames;
        this.categories = categories;
    }

    @Transactional
    public FrameResponse create(CreateFrameRequest request) {
        categories.requireCompatible(request.categoryId(), CategoryItemType.FRAME);
        Frame frame = new Frame();
        apply(
                frame,
                request.model(),
                request.color(),
                request.material(),
                request.price(),
                request.stockQty(),
                request.categoryId());
        frame.setActive(request.active() == null || request.active());
        return FrameResponse.from(frames.save(frame));
    }

    @Transactional
    public FrameResponse update(long id, UpdateFrameRequest request) {
        categories.requireCompatible(request.categoryId(), CategoryItemType.FRAME);
        Frame frame = frames.findById(id).orElseThrow(() -> new ResourceNotFoundException("Frame not found."));
        apply(
                frame,
                request.model(),
                request.color(),
                request.material(),
                request.price(),
                request.stockQty(),
                request.categoryId());
        frame.setActive(request.active() == null || request.active());
        return FrameResponse.from(frames.save(frame));
    }

    /**
     * Removes a frame from the shop wall by deactivating it, not by deleting the
     * row: an order or a stock entry may still reference it, and V3's
     * {@code ON DELETE RESTRICT} is the database saying so. An already-inactive
     * frame is left inactive, so calling this twice is harmless. Reactivating is
     * an ordinary {@link #update}.
     */
    @Transactional
    public FrameResponse discontinue(long id) {
        Frame frame = frames.findById(id).orElseThrow(() -> new ResourceNotFoundException("Frame not found."));
        frame.setActive(false);
        return FrameResponse.from(frames.save(frame));
    }

    @Transactional(readOnly = true)
    public FrameResponse read(long id) {
        return frames.findById(id)
                .map(FrameResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Frame not found."));
    }

    /** Every frame, oldest first, for the shop's inventory list. */
    @Transactional(readOnly = true)
    public List<FrameResponse> list() {
        return frames.findAllByOrderByIdAsc().stream().map(FrameResponse::from).toList();
    }

    private static void apply(
            Frame frame, String model, String color, String material, BigDecimal price, int stockQty, Long categoryId) {
        frame.setModel(model);
        frame.setColor(color);
        frame.setMaterial(material);
        frame.setPrice(price);
        frame.setStockQty(stockQty);
        frame.setCategoryId(categoryId);
    }
}
