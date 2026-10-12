package com.myopty.catalog.controller;

import com.myopty.catalog.dto.ApiResponse;
import com.myopty.catalog.dto.StorefrontFrameResponse;
import com.myopty.catalog.service.FrameService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The public storefront's read of the frame catalogue: every frame the shop has
 * on the wall, so a visitor can browse available styles.
 *
 * <p>It sits at {@code /api/frames}, outside {@code /api/shop/**}, because browsing
 * is anonymous — the path is on the filter chain's public list. Only active frames
 * are returned, and only through {@link StorefrontFrameResponse}, so the exact
 * stock count and the shop's active flag never reach a visitor. The owner's writes
 * and its fuller view stay on {@code ShopFrameController}.
 */
@RestController
@RequestMapping("/api/frames")
public class CatalogFrameController {

    private final FrameService service;

    public CatalogFrameController(FrameService service) {
        this.service = service;
    }

    /** Every active frame, oldest first, for the shop wall. */
    @GetMapping
    public ApiResponse<List<StorefrontFrameResponse>> list() {
        return ApiResponse.of(service.listActiveForStorefront());
    }
}
