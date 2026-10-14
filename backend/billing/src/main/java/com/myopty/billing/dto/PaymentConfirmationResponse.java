package com.myopty.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The payment-confirmation screen's answer: everything the customer needs to see
 * that their money went where they meant, keyed by the gateway's transaction id.
 *
 * <p>It carries the order number, which the payment row does not store, because the
 * confirmation is read right after checkout and "here is your receipt for
 * ORD-…-…" is the whole point of the screen.
 */
public record PaymentConfirmationResponse(
        String transactionId,
        long paymentId,
        long orderId,
        String orderNumber,
        BigDecimal amount,
        BigDecimal refundedAmount,
        String status,
        String method,
        String methodName,
        LocalDateTime paidAt,
        Long invoiceId,
        String invoiceNumber) {}
