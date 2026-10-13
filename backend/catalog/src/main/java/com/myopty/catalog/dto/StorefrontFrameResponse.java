package com.myopty.catalog.dto;

import com.myopty.catalog.model.Frame;
import java.math.BigDecimal;

/**
 * What a browsing visitor gets back for a frame: the details a shop wall shows,
 * and nothing the shop keeps to itself.
 *
 * <p>It is a separate record from {@link FrameResponse} because the two answer
 * different questions. The owner needs the exact {@code stockQty}, the
 * {@code active} flag and the category to run the inventory; a visitor only needs
 * to know whether the frame can be bought, so stock is reduced to {@code inStock}.
 * Keeping the public surface its own type is what stops a later owner-only field
 * being added to {@code FrameResponse} and silently leaking onto the storefront.
 *
 * <p>{@code imageUrl} is the thumbnail the seed data carries; it may be null, and
 * the storefront renders a placeholder in that case.
 */
public record StorefrontFrameResponse(
        long id, String model, String color, String material, BigDecimal price, String imageUrl, boolean inStock) {

    public static StorefrontFrameResponse from(Frame frame) {
        return new StorefrontFrameResponse(
                frame.getId(),
                frame.getModel(),
                frame.getColor(),
                frame.getMaterial(),
                frame.getPrice(),
                frame.getImageUrl(),
                frame.getStockQty() > 0);
    }
}
