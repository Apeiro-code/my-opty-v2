package com.myopty.catalog.service;

import com.myopty.catalog.dto.CreateFrameRequest;
import com.myopty.catalog.dto.FrameResponse;
import com.myopty.catalog.dto.UpdateFrameRequest;
import com.myopty.catalog.exception.ResourceNotFoundException;
import com.myopty.catalog.model.Frame;
import com.myopty.catalog.repository.FrameRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a frame to the shop's catalogue and keeps an existing one accurate.
 *
 * <p>A new frame is saved with no category: this story's form does not choose one,
 * and V3 leaves {@code category_id} nullable so a frame can be filed later. The
 * price and stock rules are enforced by Bean Validation before the service is
 * entered, so the service only has to apply the defaults and persist.
 *
 * <p>An edit loads the row first so a missing id is a 404 rather than a silent
 * insert, then writes back the whole record: the endpoint is a PUT, so an omitted
 * field is a client mistake the request's constraints have already refused.
 */
@Service
public class FrameService {

    private final FrameRepository frames;

    public FrameService(FrameRepository frames) {
        this.frames = frames;
    }

    @Transactional
    public FrameResponse create(CreateFrameRequest request) {
        Frame frame = new Frame();
        apply(frame, request.model(), request.color(), request.material(), request.price(), request.stockQty());
        frame.setActive(request.active() == null || request.active());
        return FrameResponse.from(frames.save(frame));
    }

    @Transactional
    public FrameResponse update(long id, UpdateFrameRequest request) {
        Frame frame = frames.findById(id).orElseThrow(() -> new ResourceNotFoundException("Frame not found."));
        apply(frame, request.model(), request.color(), request.material(), request.price(), request.stockQty());
        frame.setActive(request.active() == null || request.active());
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
            Frame frame, String model, String color, String material, BigDecimal price, int stockQty) {
        frame.setModel(model);
        frame.setColor(color);
        frame.setMaterial(material);
        frame.setPrice(price);
        frame.setStockQty(stockQty);
    }
}
