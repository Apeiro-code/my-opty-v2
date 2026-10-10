package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.NotificationResponse;
import com.myopty.order.service.NotificationService;
import com.myopty.shared.user.AppUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The customer's notifications: what the shop told them about their orders.
 *
 * <p>The customer is taken from the session, never from a {@code customerId}
 * query parameter. The README lists {@code ?customerId=} as the shape, but a
 * filter a caller can set on their own request is not a security boundary — the
 * id that scopes the query has to be the one the session proved. An optional
 * {@code orderId} narrows the list, and the service scopes it by the same session
 * id, so one customer's order id cannot read another's history.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list(
            @RequestParam(required = false) Long orderId, @AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(service.listForCustomer(customer.getId(), orderId));
    }
}
