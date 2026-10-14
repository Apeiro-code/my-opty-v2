package com.myopty.billing.controller;

import com.myopty.billing.dto.ApiResponse;
import com.myopty.billing.dto.PageResponse;
import com.myopty.billing.dto.PaymentResponse;
import com.myopty.billing.dto.RefundRequest;
import com.myopty.billing.model.PaymentStatus;
import com.myopty.billing.service.PaymentService;
import com.myopty.billing.service.RefundService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop's payments: a filtered list and the refund action.
 *
 * <p>Under {@code /api/admin/**}, which the filter chain gates to the shop roles, so
 * these read and act on any customer's payments by design. The filters are all
 * optional — status, an inclusive date range, and paging — and a value the server
 * cannot convert answers a 400 through this module's advice rather than a 500.
 */
@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final PaymentService payments;
    private final RefundService refunds;

    public AdminPaymentController(PaymentService payments, RefundService refunds) {
        this.payments = payments;
        this.refunds = refunds;
    }

    @GetMapping
    public ApiResponse<PageResponse<PaymentResponse>> list(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.of(payments.listForAdmin(status, startDate, endDate, page, limit));
    }

    /** Returns all or part of a settled payment; a null amount refunds the balance. */
    @PostMapping("/{id}/refund")
    public ApiResponse<PaymentResponse> refund(
            @PathVariable long id, @RequestBody @Valid RefundRequest request, @AuthenticationPrincipal AppUser admin) {
        return ApiResponse.of(refunds.refund(id, request, admin));
    }
}
