package com.myopty.catalog.dto;

import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.LensType;
import java.math.BigDecimal;

/**
 * What a browsing visitor gets back for a lens: details for the storefront, with
 * stock reduced to {@code inStock} and no shop-only fields exposed.
 */
public record StorefrontLensResponse(
        long id, String name, LensType type, String coating, BigDecimal price, String imageUrl, boolean inStock) {

    public static StorefrontLensResponse from(Lens lens) {
        return new StorefrontLensResponse(
                lens.getId(),
                lens.getName(),
                lens.getType(),
                lens.getCoating(),
                lens.getPrice(),
                lens.getImageUrl(),
                lens.getStockQty() > 0);
    }
}
