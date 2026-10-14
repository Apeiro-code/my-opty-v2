package com.myopty.billing.service;

import java.math.BigDecimal;

/**
 * Everything a receipt or refund email needs, gathered by {@link ReceiptService}
 * so the sender only has to render it.
 *
 * <p>{@code refundedAmount} is zero on a payment receipt and the amount just
 * returned on a refund confirmation, which is what lets one record serve the one
 * sender and its two messages without the sender reaching back into the database.
 */
public record ReceiptEmail(
        String recipient,
        String customerName,
        String orderNumber,
        String invoiceNumber,
        BigDecimal total,
        BigDecimal refundedAmount,
        byte[] invoicePdf) {}
