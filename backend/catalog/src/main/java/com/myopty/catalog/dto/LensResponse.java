package com.myopty.catalog.dto;

import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.LensType;
import java.math.BigDecimal;

/**
 * What the shop gets back for a lens: the record it just saved, read back from
 * storage.
 *
 * <p>{@code active} is the shop's control over whether the lens appears in the
 * collection; an inactive lens is kept so old orders still resolve. {@code
 * categoryId} is the category it is filed under, or null while unfiled.
 */
public record LensResponse(
        long id,
        String name,
        LensType type,
        String coating,
        BigDecimal price,
        int stockQty,
        boolean active,
        Long categoryId) {

    public static LensResponse from(Lens lens) {
        return new LensResponse(
                lens.getId(),
                lens.getName(),
                lens.getType(),
                lens.getCoating(),
                lens.getPrice(),
                lens.getStockQty(),
                lens.isActive(),
                lens.getCategoryId());
    }
}
