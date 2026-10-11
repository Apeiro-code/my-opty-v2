package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.DiscountResponse;
import com.myopty.order.service.DiscountService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The storefront's read of the promotions running now, so a customer can see a
 * discounted price beside the original. Public rather than under the shop's
 * {@code /api/shop/**} surface: browsing is anonymous, and a promotion's name,
 * percentage and targets are what the customer is being shown anyway.
 *
 * <p>It returns only what is running today, newest first. The shape is the same
 * {@link DiscountResponse} the shop reads — one record serves both surfaces, and
 * the fields the storefront does not draw (the creating client, the ended
 * timestamp) carry no secret.
 */
@RestController
@RequestMapping("/api/discounts")
public class PublicDiscountController {

    private final DiscountService service;

    public PublicDiscountController(DiscountService service) {
        this.service = service;
    }

    @GetMapping("/active")
    public ApiResponse<List<DiscountResponse>> active() {
        return ApiResponse.of(service.list(true, null));
    }
}
