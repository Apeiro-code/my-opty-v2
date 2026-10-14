package com.myopty.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * The body of {@code POST /api/payments/process}: which order to pay for, by which
 * method, and whatever provider fields the client collected.
 *
 * <p>The amount is deliberately absent. It is computed server-side from the order
 * and the catalogue, so a client cannot name its own price; a {@code total} field
 * here could only be used to underpay.
 *
 * <p>{@code paymentDetails} is the provider's own vocabulary — a card token, a
 * PayHere return payload — passed through to the gateway untouched, which is why
 * it is an open map rather than a typed shape billing would have to keep in step
 * with every provider.
 */
public record ProcessPaymentRequest(
        @NotNull(message = "Choose an order to pay for.") Long orderId,

        @NotBlank(message = "Choose a payment method.") String paymentMethod,

        Map<String, String> paymentDetails) {}
