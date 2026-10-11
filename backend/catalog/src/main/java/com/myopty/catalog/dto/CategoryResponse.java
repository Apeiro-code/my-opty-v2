package com.myopty.catalog.dto;

import com.myopty.catalog.model.Category;
import com.myopty.catalog.model.CategoryItemType;

/**
 * What the shop gets back for a category: enough to render the filing picker and
 * to decide which records it belongs on.
 *
 * <p>{@code itemType} is exposed so the shop's frame form can offer only frame
 * categories and the lens form only lens ones, without a second request.
 */
public record CategoryResponse(long id, String name, String slug, CategoryItemType itemType) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getItemType());
    }
}
