package com.myopty.catalog.controller;

import com.myopty.catalog.dto.ApiResponse;
import com.myopty.catalog.dto.CreateFrameRequest;
import com.myopty.catalog.dto.FrameResponse;
import com.myopty.catalog.dto.UpdateFrameRequest;
import com.myopty.catalog.service.FrameService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's frame catalogue: add a frame so it appears on the site, and
 * edit one so its details stay accurate.
 *
 * <p>Everything sits under {@code /api/shop/**} so the {@code ROLE_CLIENT} rule
 * the filter chain already applies is the only place the role is checked; a
 * customer never reaches these endpoints. The customer-facing reads live on
 * {@code /api/frames} with the browsing story, so this controller never has to
 * decide what a visitor may see.
 */
@RestController
@RequestMapping("/api/shop/frames")
public class ShopFrameController {

    private final FrameService service;

    public ShopFrameController(FrameService service) {
        this.service = service;
    }

    /** Every frame, oldest first, for the inventory list. */
    @GetMapping
    public ApiResponse<List<FrameResponse>> list() {
        return ApiResponse.of(service.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FrameResponse> create(@RequestBody @Valid CreateFrameRequest request) {
        return ApiResponse.of(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<FrameResponse> update(@PathVariable long id, @RequestBody @Valid UpdateFrameRequest request) {
        return ApiResponse.of(service.update(id, request));
    }

    /**
     * Takes a frame off the shop wall. It is a discontinue, not a row delete: the
     * record stays so old orders still resolve, and the edit endpoint is how it is
     * put back.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<FrameResponse> discontinue(@PathVariable long id) {
        return ApiResponse.of(service.discontinue(id));
    }
}
