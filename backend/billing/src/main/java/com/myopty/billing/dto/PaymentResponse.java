package com.myopty.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A payment as the shop and the customer see it: what was charged, by which
 * method, where it stands, and the invoice it produced.
 *
 * <p>{@code refundedAmount} sits beside {@code amount} rather than replacing it, so
 * a partially refunded payment still shows what was originally taken. {@code method}
 * and {@code invoiceNumber} are resolved for display; both are null when the lookup
 * has nothing to add, and the global {@code non_null} inclusion drops them.
 */
public record PaymentResponse(
        long id,
        long orderId,
        long customerId,
        String method,
        String methodName,
        BigDecimal amount,
        BigDecimal refundedAmount,
        String status,
        String gatewayTransactionId,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        String invoiceNumber) {}
