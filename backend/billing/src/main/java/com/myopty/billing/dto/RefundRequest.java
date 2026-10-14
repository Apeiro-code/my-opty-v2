package com.myopty.billing.dto;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * The body of {@code POST /api/admin/payments/{id}/refund}: how much to return and
 * why.
 *
 * <p>{@code amount} is optional — leaving it null refunds everything still
 * outstanding — which is why it cannot carry a {@code @Positive} constraint: a
 * supplied value is checked against the payment in the service, where the remaining
 * balance is known. A supplied zero or negative value is refused there too.
 */
public record RefundRequest(
        BigDecimal amount,

        @Size(max = 500, message = "A refund reason cannot be longer than 500 characters.")
        String reason) {}
