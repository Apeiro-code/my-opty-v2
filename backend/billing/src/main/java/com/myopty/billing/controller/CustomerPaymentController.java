package com.myopty.billing.controller;

import com.myopty.billing.dto.ApiResponse;
import com.myopty.billing.dto.PaymentResponse;
import com.myopty.billing.service.PaymentService;
import com.myopty.shared.user.AppUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A customer's own payment history, for the billing page.
 *
 * <p>Scoped to the session's customer id, so the list cannot be another customer's.
 * It lives at its own path rather than on {@code /api/payments} so the customer's
 * history is not confused with the shop's list, which sits under {@code /api/admin}.
 */
@RestController
@RequestMapping("/api/customer/payments")
public class CustomerPaymentController {

    private final PaymentService payments;

    public CustomerPaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> list(@AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(payments.listOwn(customer.getId()));
    }
}
