package com.myopty.billing.controller;

import com.myopty.billing.dto.ApiResponse;
import com.myopty.billing.dto.PaymentMethodResponse;
import com.myopty.billing.service.PaymentMethodService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The checkout's list of accepted payment methods.
 *
 * <p>One read, no body: the list is the same for every caller and the filter chain
 * has already required a session, so there is nothing per-customer to resolve here.
 */
@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethods;

    public PaymentMethodController(PaymentMethodService paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    @GetMapping
    public ApiResponse<List<PaymentMethodResponse>> list() {
        return ApiResponse.of(paymentMethods.listActive());
    }
}
