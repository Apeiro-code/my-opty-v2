package com.myopty.billing.dto;

import com.myopty.billing.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * An invoice as returned to an owner or the shop: the money as it was at issue, and
 * the number to quote.
 *
 * <p>{@code pdfAvailable} tells the client whether the {@code /pdf} endpoint has
 * anything to render without it having to guess; the four money fields together are
 * the {@code subtotal - discount + tax == total} reconciliation the invoice is.
 */
public record InvoiceResponse(
        long id,
        String invoiceNumber,
        long paymentId,
        long orderId,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal total,
        LocalDateTime issuedAt,
        boolean pdfAvailable) {

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getPaymentId(),
                invoice.getOrderId(),
                invoice.getSubtotal(),
                invoice.getDiscountAmount(),
                invoice.getTaxAmount(),
                invoice.getTotal(),
                invoice.getIssuedAt(),
                invoice.getPdfObjectKey() != null);
    }
}
