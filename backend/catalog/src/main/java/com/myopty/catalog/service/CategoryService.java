package com.myopty.catalog.service;

import com.myopty.catalog.dto.CategoryResponse;
import com.myopty.catalog.exception.InvalidCategoryException;
import com.myopty.catalog.model.Category;
import com.myopty.catalog.model.CategoryItemType;
import com.myopty.catalog.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lists the categories a frame or lens may be filed under, and confirms a chosen
 * one is allowed before a save.
 *
 * <p>This story files records into the categories the shop already has; it does
 * not create them. The category table is seeded in V7 and every category is a
 * plain readable row, so the service only reads.
 */
@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    /** Every category, oldest first, for the filing picker. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categories.findAllByOrderByIdAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    /**
     * Confirms a chosen category may hold the type of record being saved.
     *
     * <p>A null id means "leave it unfiled", which the nullable {@code category_id}
     * columns allow, so it passes. A category that does not exist, or one whose
     * {@code item_type} is for the other product, is a client mistake and is
     * refused before the write rather than by the foreign key afterwards.
     */
    @Transactional(readOnly = true)
    public void requireCompatible(Long categoryId, CategoryItemType expected) {
        if (categoryId == null) {
            return;
        }
        Category category = categories
                .findById(categoryId)
                .orElseThrow(() -> new InvalidCategoryException("The chosen category does not exist."));
        if (!category.getItemType().allows(expected)) {
            throw new InvalidCategoryException("The chosen category is not for " + label(expected) + ".");
        }
    }

    private static String label(CategoryItemType type) {
        return type == CategoryItemType.LENS ? "lenses" : "frames";
    }
}
