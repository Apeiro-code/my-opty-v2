package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.PrescriptionResponse;
import com.myopty.order.dto.RejectRequest;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.service.PrescriptionReviewService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shop owner's prescription review: the pending queue and the verify/reject
 * decisions.
 *
 * <p>Everything is mounted under {@code /api/shop/**}, which the shared module's
 * filter chain already restricts to {@code ROLE_CLIENT} — so the role rule lives
 * in one place and this controller does not repeat it. The customer's own
 * prescription endpoints stay on {@code /api/prescriptions} and are untouched.
 *
 * <p>The deciding client is read from the session, never the body, and is stamped
 * on the decision so it can be traced back.
 */
@RestController
@RequestMapping("/api/shop/prescriptions")
public class ShopPrescriptionController {

    private final PrescriptionReviewService service;

    public ShopPrescriptionController(PrescriptionReviewService service) {
        this.service = service;
    }

    /** The review queue; {@code status} defaults to {@code PENDING_REVIEW}. */
    @GetMapping
    public ApiResponse<List<PrescriptionResponse>> queue(@RequestParam(required = false) VerificationStatus status) {
        return ApiResponse.of(service.queue(status));
    }

    @PutMapping("/{id}/verify")
    public ApiResponse<PrescriptionResponse> verify(@PathVariable long id, @AuthenticationPrincipal AppUser client) {
        return ApiResponse.of(service.verify(client.getId(), id));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<PrescriptionResponse> reject(
            @PathVariable long id, @RequestBody @Valid RejectRequest request, @AuthenticationPrincipal AppUser client) {
        return ApiResponse.of(service.reject(client.getId(), id, request.reason()));
    }
}
