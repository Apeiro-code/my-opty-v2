package com.myopty.catalog.service;

import com.myopty.catalog.dto.CreateLensRequest;
import com.myopty.catalog.dto.LensResponse;
import com.myopty.catalog.dto.UpdateLensRequest;
import com.myopty.catalog.exception.ResourceNotFoundException;
import com.myopty.catalog.model.CategoryItemType;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.LensType;
import com.myopty.catalog.repository.LensRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a lens to the shop's collection and keeps an existing one accurate.
 *
 * <p>A lens may be filed under a category or left unfiled: the V4 column is
 * nullable, so a null {@code categoryId} is a valid choice and only a non-null
 * one is checked against the category's {@code item_type}. The price and stock
 * rules are enforced by Bean Validation before the service is entered, so the
 * service only has to apply the default and persist.
 *
 * <p>An edit loads the row first so a missing id is a 404 rather than a silent
 * insert, then writes back the whole record: the endpoint is a PUT, so an omitted
 * field is a client mistake the request's constraints have already refused.
 */
@Service
public class LensService {

    private final LensRepository lenses;
    private final CategoryService categories;

    public LensService(LensRepository lenses, CategoryService categories) {
        this.lenses = lenses;
        this.categories = categories;
    }

    @Transactional
    public LensResponse create(CreateLensRequest request) {
        categories.requireCompatible(request.categoryId(), CategoryItemType.LENS);
        Lens lens = new Lens();
        apply(
                lens,
                request.name(),
                request.type(),
                request.coating(),
                request.price(),
                request.stockQty(),
                request.categoryId());
        lens.setActive(request.active() == null || request.active());
        return LensResponse.from(lenses.save(lens));
    }

    @Transactional
    public LensResponse update(long id, UpdateLensRequest request) {
        categories.requireCompatible(request.categoryId(), CategoryItemType.LENS);
        Lens lens = lenses.findById(id).orElseThrow(() -> new ResourceNotFoundException("Lens not found."));
        apply(
                lens,
                request.name(),
                request.type(),
                request.coating(),
                request.price(),
                request.stockQty(),
                request.categoryId());
        lens.setActive(request.active() == null || request.active());
        return LensResponse.from(lenses.save(lens));
    }

    /** Every lens, oldest first, for the shop's collection list. */
    @Transactional(readOnly = true)
    public List<LensResponse> list() {
        return lenses.findAllByOrderByIdAsc().stream().map(LensResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<com.myopty.catalog.dto.StorefrontLensResponse> listActiveForStorefront(String type) {
        if (type == null || type.trim().isEmpty()) {
            return lenses.findAllByActiveTrueOrderByIdAsc().stream()
                    .map(com.myopty.catalog.dto.StorefrontLensResponse::from)
                    .toList();
        }
        try {
            com.myopty.catalog.model.LensType lensType =
                    com.myopty.catalog.model.LensType.valueOf(type.trim().toUpperCase());
            return lenses.findAllByActiveTrueAndTypeOrderByIdAsc(lensType).stream()
                    .map(com.myopty.catalog.dto.StorefrontLensResponse::from)
                    .toList();
        } catch (IllegalArgumentException e) {
            return java.util.Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<com.myopty.catalog.dto.StorefrontLensResponse> listActiveForStorefront() {
        return listActiveForStorefront(null);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.myopty.catalog.dto.StorefrontLensResponse> listNewArrivals(int limit) {
        int n = limit > 0 ? limit : 8;
        return lenses.findNewArrivals(n).stream()
                .map(com.myopty.catalog.dto.StorefrontLensResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public com.myopty.catalog.dto.StorefrontLensDetailResponse getActiveForStorefront(long id) {
        return lenses.findByIdAndActiveTrue(id)
                .map(com.myopty.catalog.dto.StorefrontLensDetailResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Lens not found."));
    }

    private static void apply(
            Lens lens, String name, LensType type, String coating, BigDecimal price, int stockQty, Long categoryId) {
        lens.setName(name);
        lens.setType(type);
        lens.setCoating(coating);
        lens.setPrice(price);
        lens.setStockQty(stockQty);
        lens.setCategoryId(categoryId);
    }
}
