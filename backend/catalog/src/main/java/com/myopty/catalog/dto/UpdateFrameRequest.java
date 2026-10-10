package com.myopty.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * The body of {@code PUT /api/shop/frames/{id}}: the frame as it should now be.
 *
 * <p>A full replacement rather than a patch — the edit form sends every field it
 * shows, so a request that omits one is a client mistake and is refused rather
 * than silently leaving the old value. It is a separate record from
 * {@link CreateFrameRequest} because the two are free to diverge: an edit may one
 * day allow a field creation does not, or vice versa.
 */
public record UpdateFrameRequest(
        @NotBlank(message = "Enter a model name.") @Size(max = 160, message = "Model name is too long.")
        String model,

        @Size(max = 120, message = "Colour is too long.") String color,

        @NotBlank(message = "Enter a material.") @Size(max = 120, message = "Material is too long.")
        String material,

        @NotNull(message = "Enter a price.") @DecimalMin(value = "0.0", message = "Price cannot be negative.")
        BigDecimal price,

        @NotNull(message = "Enter a stock quantity.") @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQty,

        Boolean active) {}
