package com.myopty.catalog.dto;

import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.LensType;
import java.math.BigDecimal;

/**
 * Detailed public view of a single lens for the storefront.
 */
public record StorefrontLensDetailResponse(
        long id,
        String name,
        LensType type,
        String coating,
        BigDecimal price,
        String imageUrl,
        String description,
        boolean inStock) {

    public static StorefrontLensDetailResponse from(Lens lens) {
        return new StorefrontLensDetailResponse(
                lens.getId(),
                lens.getName(),
                lens.getType(),
                lens.getCoating(),
                lens.getPrice(),
                lens.getImageUrl(),
                lens.getDescription(),
                lens.getStockQty() > 0);
    }
}
