package com.myopty.order.dto;

import com.myopty.order.model.OrderType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * The body of {@code POST /api/orders}: which prescription to build from, the
 * selected order type, and the frame and lens to link it to.
 *
 * <p>The customer is never a field. The controller reads it from the session, so
 * a {@code customerId} here could only be used to misfile an order against
 * someone else.
 *
 * <p>{@code frameId} is intentionally unconstrained: a customer may order lenses
 * only (V101 makes the column nullable). {@code lensId} is required because the
 * order type is the lens type, and a progressive order without a lens has nothing
 * to route to the progressive workflow.
 */
public record CreateOrderRequest(
        @NotNull(message = "Choose a prescription.") Long prescriptionId,

        @NotNull(message = "Choose an order type.") OrderType orderType,

        @NotNull(message = "Choose a lens.") Long lensId,

        Long frameId,

        @Positive(message = "Quantity must be at least one.")
        Integer quantity) {}
