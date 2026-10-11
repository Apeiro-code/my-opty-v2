package com.myopty.catalog.controller;

import com.myopty.catalog.dto.ApiResponse;
import com.myopty.catalog.dto.CreateLensRequest;
import com.myopty.catalog.dto.LensResponse;
import com.myopty.catalog.dto.UpdateLensRequest;
import com.myopty.catalog.service.LensService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's lens collection: add a lens so it appears in the collection,
 * and edit one so its details stay accurate.
 *
 * <p>Everything sits under {@code /api/shop/**} so the {@code ROLE_CLIENT} rule
 * the filter chain already applies is the only place the role is checked; a
 * customer never reaches these endpoints. The customer-facing reads live on
 * {@code /api/lenses} with the browsing story, so this controller never has to
 * decide what a visitor may see.
 */
@RestController
@RequestMapping("/api/shop/lenses")
public class ShopLensController {

    private final LensService service;

    public ShopLensController(LensService service) {
        this.service = service;
    }

    /** Every lens, oldest first, for the collection list. */
    @GetMapping
    public ApiResponse<List<LensResponse>> list() {
        return ApiResponse.of(service.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LensResponse> create(@RequestBody @Valid CreateLensRequest request) {
        return ApiResponse.of(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<LensResponse> update(@PathVariable long id, @RequestBody @Valid UpdateLensRequest request) {
        return ApiResponse.of(service.update(id, request));
    }
}
