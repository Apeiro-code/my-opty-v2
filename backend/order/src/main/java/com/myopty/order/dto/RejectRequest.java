package com.myopty.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The body of a rejection: why the shop refused a prescription or an order.
 *
 * <p>The reason is required and length-capped to the column width (V100 and V101
 * both declare {@code rejection_reason VARCHAR(500)}), because a rejection is
 * what "flag missing details" means: the customer is told exactly what to fix, and
 * an empty string would tell them nothing.
 */
public record RejectRequest(
        @NotBlank(message = "Say why the request is being rejected.") @Size(max = 500)
        String reason) {}
