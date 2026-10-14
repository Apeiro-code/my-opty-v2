package com.myopty.billing.controller;

import com.myopty.billing.dto.ApiResponse;
import com.myopty.billing.dto.InvoiceResponse;
import com.myopty.billing.service.InvoiceService;
import com.myopty.shared.user.AppUser;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reading an invoice: its data, and its PDF.
 *
 * <p>Both are ownership-checked in the service — a customer sees only their own, the
 * shop sees any — and both answer 404 for a missing invoice and for someone else's,
 * so the difference cannot be probed.
 *
 * <p>The PDF is streamed rather than stored: the renderer builds it from the invoice
 * row, so there is no file to keep in step. The filename is the invoice number, which
 * is what a person expects to see when it lands in their downloads.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoices;

    public InvoiceController(InvoiceService invoices) {
        this.invoices = invoices;
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> read(@PathVariable long id, @AuthenticationPrincipal AppUser caller) {
        return ApiResponse.of(invoices.read(id, caller));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable long id, @AuthenticationPrincipal AppUser caller) {
        InvoiceResponse invoice = invoices.read(id, caller);
        byte[] pdf = invoices.renderPdf(id, caller);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(invoice.invoiceNumber() + ".pdf")
                                .build()
                                .toString())
                .body(pdf);
    }
}
