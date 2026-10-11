package com.myopty.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * The body of {@code POST /api/shop/frames}: the details of a new frame.
 *
 * <p>{@code model} and {@code material} are required because a frame without them
 * is not something a customer can search for or identify; {@code color} is
 * optional because it is a variant, not an identity. {@code price} and
 * {@code stockQty} are required and cannot be negative — the table's
 * {@code CHECK} constraints say the same thing, but catching it here returns the
 * error envelope instead of a constraint failure.
 *
 * <p>{@code active} is a {@link Boolean} rather than a primitive so that omitting
 * it means "on sale", not "hidden"; the service applies that default. A frame is
 * deactivated instead of deleted so it can still be referenced by old orders.
 *
 * <p>{@code categoryId} is the filing choice. It is nullable so a frame can be
 * created unfiled, which the V3 column allows.
 */
public record CreateFrameRequest(
        @NotBlank(message = "Enter a model name.") @Size(max = 160, message = "Model name is too long.")
        String model,

        @Size(max = 120, message = "Colour is too long.") String color,

        @NotBlank(message = "Enter a material.") @Size(max = 120, message = "Material is too long.")
        String material,

        @NotNull(message = "Enter a price.") @DecimalMin(value = "0.0", message = "Price cannot be negative.")
        BigDecimal price,

        @NotNull(message = "Enter a stock quantity.") @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQty,

        Boolean active,

        Long categoryId) {}
