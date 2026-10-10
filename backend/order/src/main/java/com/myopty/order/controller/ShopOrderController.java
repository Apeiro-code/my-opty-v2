package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.OrderResponse;
import com.myopty.order.dto.RejectRequest;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.service.OrderReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's order approval: the pending queue and the approve/reject
 * decisions that decide whether an order reaches production.
 *
 * <p>Like {@link ShopPrescriptionController}, everything sits under
 * {@code /api/shop/**} so the {@code ROLE_CLIENT} rule the filter chain already
 * applies is the only place the role is checked. The customer's create and read
 * endpoints stay on {@code /api/orders} and are untouched.
 *
 * <p>Approving is refused while the linked prescription has not been verified; the
 * service owns that rule, and the customer principal is not needed here because
 * the order table records the reason but not the deciding client.
 */
@RestController
@RequestMapping("/api/shop/orders")
public class ShopOrderController {

    private final OrderReviewService service;

    public ShopOrderController(OrderReviewService service) {
        this.service = service;
    }

    /** The approval queue; {@code status} defaults to {@code PENDING}. */
    @GetMapping
    public ApiResponse<List<OrderResponse>> queue(@RequestParam(required = false) OrderStatus status) {
        return ApiResponse.of(service.queue(status));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<OrderResponse> approve(@PathVariable long id) {
        return ApiResponse.of(service.approve(id));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<OrderResponse> reject(@PathVariable long id, @RequestBody @Valid RejectRequest request) {
        return ApiResponse.of(service.reject(id, request.reason()));
    }
}
