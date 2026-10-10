package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.CreateOrderRequest;
import com.myopty.order.dto.OrderResponse;
import com.myopty.order.service.OrderService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Placing an order: linking a prescription to the frame and lens it is made with.
 *
 * <p>The customer is read from the session, never from the body, for the reason
 * {@link PrescriptionController} gives: a {@code customerId} field would let one
 * customer order against another's prescription. The service additionally scopes
 * every query by that id.
 *
 * <p>It also has to read its own orders back, so this controller serves a single
 * order by id too. The listing the README describes ({@code GET /api/orders}
 * filtered by status or prescription) is the client's queue story and is not added
 * here.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> create(
            @RequestBody @Valid CreateOrderRequest request, @AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(service.create(customer.getId(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> read(@PathVariable long id, @AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(service.readOwn(customer.getId(), id));
    }
}
