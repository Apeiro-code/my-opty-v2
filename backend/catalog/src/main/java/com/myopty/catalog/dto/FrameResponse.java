package com.myopty.catalog.dto;

import com.myopty.catalog.model.Frame;
import java.math.BigDecimal;

/**
 * What the shop gets back for a frame: the record it just saved, read back from
 * storage.
 *
 * <p>{@code active} is the shop's control over whether the frame is on the wall;
 * an inactive frame is kept so old orders still resolve, which is why it is
 * reported here rather than filtered out. {@code categoryId} is the category it
 * is filed under, or null while unfiled.
 */
public record FrameResponse(
        long id,
        String model,
        String color,
        String material,
        BigDecimal price,
        int stockQty,
        boolean active,
        Long categoryId) {

    public static FrameResponse from(Frame frame) {
        return new FrameResponse(
                frame.getId(),
                frame.getModel(),
                frame.getColor(),
                frame.getMaterial(),
                frame.getPrice(),
                frame.getStockQty(),
                frame.isActive(),
                frame.getCategoryId());
    }
}
