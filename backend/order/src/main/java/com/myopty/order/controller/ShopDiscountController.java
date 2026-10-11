package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.DiscountRequest;
import com.myopty.order.dto.DiscountResponse;
import com.myopty.order.model.DiscountState;
import com.myopty.order.service.DiscountService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's promotions: create a discount, edit one, read the history, and
 * end one before its end date.
 *
 * <p>Mounted under {@code /api/shop/**} like the rest of the shop's surface, so
 * the {@code ROLE_CLIENT} rule the filter chain already applies is the only place
 * the role is checked. The client is read from the session for the promotion's
 * {@code created_by}, never from the body, for the reason the order controller
 * gives: a field a caller sets can only misfile the record.
 *
 * <p>Ending is a {@code DELETE} because that is what the caller intends — the
 * promotion stops — even though the row is kept and only flagged ended, so the
 * history still resolves. The response is the ended discount, so a caller sees
 * the state it wound up in.
 */
@RestController
@RequestMapping("/api/shop/discounts")
public class ShopDiscountController {

    private final DiscountService service;

    public ShopDiscountController(DiscountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DiscountResponse> create(
            @RequestBody @Valid DiscountRequest request, @AuthenticationPrincipal AppUser client) {
        return ApiResponse.of(service.create(client.getId(), request));
    }

    /**
     * The history, newest first. {@code active} narrows it to what is running now
     * (or the rest); {@code state} narrows it to one derived state, for a caller
     * that would rather the server did the filtering. Both are optional and may be
     * combined.
     */
    @GetMapping
    public ApiResponse<List<DiscountResponse>> list(
            @RequestParam(required = false) Boolean active, @RequestParam(required = false) DiscountState state) {
        return ApiResponse.of(service.list(active, state));
    }

    @PutMapping("/{id}")
    public ApiResponse<DiscountResponse> update(@PathVariable long id, @RequestBody @Valid DiscountRequest request) {
        return ApiResponse.of(service.update(id, request));
    }

    /** Ends the discount early. */
    @DeleteMapping("/{id}")
    public ApiResponse<DiscountResponse> end(@PathVariable long id) {
        return ApiResponse.of(service.end(id));
    }
}
