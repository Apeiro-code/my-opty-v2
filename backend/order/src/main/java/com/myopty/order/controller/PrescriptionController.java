package com.myopty.order.controller;

import com.myopty.order.dto.ApiResponse;
import com.myopty.order.dto.PrescriptionResponse;
import com.myopty.order.dto.PrescriptionSubmission;
import com.myopty.order.service.PrescriptionService;
import com.myopty.shared.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Submitting a prescription: the customer's optical values and, optionally, a
 * scan or photo of the paper one.
 *
 * <p>The request is {@code multipart/form-data}, not JSON, so a document and the
 * values travel in one call: the {@code prescription} part carries the values as
 * JSON, the {@code document} part the bytes. {@code consumes} makes that the
 * whole contract — any other content type is answered 415 before this method is
 * entered, which is why the class does not also check it.
 *
 * <p>The customer is read from the session, never from the body: a
 * {@code customerId} field would let one customer file a prescription against
 * another, and the filter chain has already guaranteed an authenticated principal
 * before this method runs.
 */
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService service;

    public PrescriptionController(PrescriptionService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PrescriptionResponse> submit(
            @RequestPart("prescription") @Valid PrescriptionSubmission submission,
            @RequestPart(value = "document", required = false) MultipartFile document,
            @AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(service.submit(customer.getId(), submission, document));
    }

    /**
     * The caller's own prescriptions, so the order form can offer one to link.
     * An array even for one or none, matching the README's list-endpoint shape.
     */
    @GetMapping
    public ApiResponse<List<PrescriptionResponse>> list(@AuthenticationPrincipal AppUser customer) {
        return ApiResponse.of(service.listFor(customer.getId()));
    }
}
