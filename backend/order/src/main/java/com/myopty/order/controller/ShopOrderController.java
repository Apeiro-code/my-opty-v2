package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.NotificationResponse;
import com.myopty.order.dto.OrderResponse;
import com.myopty.order.dto.ReceiveDateRequest;
import com.myopty.order.dto.RejectRequest;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.service.NotificationService;
import com.myopty.order.service.OrderFulfilmentService;
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
 * The shop owner's order surface: the pending queue and its approve/reject
 * decision, the production line that follows, and the customer notifications each
 * move produced.
 *
 * <p>Like {@link ShopPrescriptionController}, everything sits under
 * {@code /api/shop/**} so the {@code ROLE_CLIENT} rule the filter chain already
 * applies is the only place the role is checked. The customer's create, list and
 * read endpoints stay on {@code /api/orders} and are untouched.
 *
 * <p>Approving is refused while the linked prescription has not been verified; the
 * service owns that rule, and the customer principal is not needed here because
 * the order table records the reason but not the deciding client.
 */
@RestController
@RequestMapping("/api/shop/orders")
public class ShopOrderController {

    private final OrderReviewService reviewService;
    private final OrderFulfilmentService fulfilmentService;
    private final NotificationService notificationService;

    public ShopOrderController(
            OrderReviewService reviewService,
            OrderFulfilmentService fulfilmentService,
            NotificationService notificationService) {
        this.reviewService = reviewService;
        this.fulfilmentService = fulfilmentService;
        this.notificationService = notificationService;
    }

    /** The approval queue; {@code status} defaults to {@code PENDING}. */
    @GetMapping
    public ApiResponse<List<OrderResponse>> queue(@RequestParam(required = false) OrderStatus status) {
        return ApiResponse.of(reviewService.queue(status));
    }

    /** Orders still in the shop: approved, processing or ready, oldest first. */
    @GetMapping("/active")
    public ApiResponse<List<OrderResponse>> active() {
        return ApiResponse.of(fulfilmentService.activeQueue());
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<OrderResponse> approve(@PathVariable long id) {
        return ApiResponse.of(reviewService.approve(id));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<OrderResponse> reject(@PathVariable long id, @RequestBody @Valid RejectRequest request) {
        return ApiResponse.of(reviewService.reject(id, request.reason()));
    }

    /** The order is now in the lab. */
    @PutMapping("/{id}/processing")
    public ApiResponse<OrderResponse> processing(@PathVariable long id) {
        return ApiResponse.of(fulfilmentService.advance(id, OrderStatus.PROCESSING));
    }

    /** The order is ready for the customer to collect. */
    @PutMapping("/{id}/ready")
    public ApiResponse<OrderResponse> ready(@PathVariable long id) {
        return ApiResponse.of(fulfilmentService.advance(id, OrderStatus.READY));
    }

    /** The order has left the shop. */
    @PutMapping("/{id}/dispatched")
    public ApiResponse<OrderResponse> dispatched(@PathVariable long id) {
        return ApiResponse.of(fulfilmentService.advance(id, OrderStatus.DISPATCHED));
    }

    /**
     * Corrects or withdraws the estimated receive date. A null {@code receiveDate}
     * withdraws it rather than setting today.
     */
    @PutMapping("/{id}/receive-date")
    public ApiResponse<OrderResponse> receiveDate(@PathVariable long id, @RequestBody ReceiveDateRequest request) {
        return ApiResponse.of(fulfilmentService.updateReceiveDate(id, request.receiveDate()));
    }

    /** What the customer was told about this order, newest first. */
    @GetMapping("/{id}/notifications")
    public ApiResponse<List<NotificationResponse>> notifications(@PathVariable long id) {
        return ApiResponse.of(notificationService.listForOrder(id));
    }
}
