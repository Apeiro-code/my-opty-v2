package com.myopty.catalog.controller;

import com.myopty.catalog.dto.ApiResponse;
import com.myopty.catalog.dto.CategoryResponse;
import com.myopty.catalog.service.CategoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's filing categories: the list a frame or lens is filed under.
 *
 * <p>Everything sits under {@code /api/shop/**} so the {@code ROLE_CLIENT} rule
 * the filter chain already applies is the only place the role is checked. The
 * customer-facing read lives on {@code /api/categories} with the browsing story,
 * which is not built yet, so this controller never has to decide what a visitor
 * may see.
 */
@RestController
@RequestMapping("/api/shop/categories")
public class ShopCategoryController {

    private final CategoryService service;

    public ShopCategoryController(CategoryService service) {
        this.service = service;
    }

    /** Every category, oldest first, for the filing picker. */
    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.of(service.list());
    }
}
