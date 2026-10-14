package com.myopty.billing.controller;

import com.myopty.billing.dto.ApiResponse;
import com.myopty.billing.dto.PaymentConfirmationResponse;
import com.myopty.billing.dto.PaymentResponse;
import com.myopty.billing.dto.ProcessPaymentRequest;
import com.myopty.billing.service.PaymentService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The customer's checkout payments: take one, read its confirmation, confirm a cash
 * handover.
 *
 * <p>The customer comes from the session, never the body, so a request cannot pay
 * for another customer's order; the service prices the order against that same id.
 * The confirmation is keyed by the gateway's transaction id — the reference the
 * checkout holds after a redirect — and is scoped to the caller.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/process")
    public ApiResponse<PaymentResponse> process(
            @RequestBody @Valid ProcessPaymentRequest request, @AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(payments.process(customer.getId(), request));
    }

    /** The receipt screen, read by the transaction id the gateway returned. */
    @GetMapping("/confirmation/{transactionId}")
    public ApiResponse<PaymentConfirmationResponse> confirmation(
            @PathVariable String transactionId, @AuthenticationPrincipal AppUser caller) {
        return ApiResponse.of(payments.readConfirmation(caller, transactionId));
    }

    /** Confirms a cash-on-pickup payment once the money has been handed over. */
    @PutMapping("/{id}/capture")
    public ApiResponse<PaymentResponse> capture(@PathVariable long id, @AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(payments.confirmCash(customer.getId(), id));
    }
}
