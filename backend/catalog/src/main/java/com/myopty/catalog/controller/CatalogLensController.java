package com.myopty.catalog.controller;

import com.myopty.catalog.dto.ApiResponse;
import com.myopty.catalog.dto.StorefrontLensResponse;
import com.myopty.catalog.service.LensService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The public storefront's read of the lens catalogue: every active lens the shop has
 * on offer, so a visitor can browse options.
 *
 * <p>It sits at {@code /api/lenses}, outside {@code /api/shop/**}, and is on the
 * public path list.
 */
@RestController
@RequestMapping("/api/lenses")
public class CatalogLensController {

    private final LensService service;

    public CatalogLensController(LensService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<StorefrontLensResponse>> list(
            @org.springframework.web.bind.annotation.RequestParam(value = "type", required = false) String type) {
        return ApiResponse.of(service.listActiveForStorefront(type));
    }

    @GetMapping("/new-arrivals")
    public ApiResponse<List<StorefrontLensResponse>> newArrivals(
            @org.springframework.web.bind.annotation.RequestParam(value = "limit", required = false, defaultValue = "8")
                    int limit) {
        return ApiResponse.of(service.listNewArrivals(limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<com.myopty.catalog.dto.StorefrontLensDetailResponse> get(
            @org.springframework.web.bind.annotation.PathVariable long id) {
        return ApiResponse.of(service.getActiveForStorefront(id));
    }
}
