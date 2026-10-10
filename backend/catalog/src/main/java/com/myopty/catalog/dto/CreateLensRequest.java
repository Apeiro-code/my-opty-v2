package com.myopty.catalog.dto;

import com.myopty.catalog.model.LensType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * The body of {@code POST /api/shop/lenses}: the details of a new lens.
 *
 * <p>{@code name} and {@code type} are required. The name is how a customer
 * identifies the lens, and the type is what an order routes on, so neither can be
 * left out. {@code coating} is optional — a lens may be sold uncoated. {@code price}
 * and {@code stockQty} cannot be negative, which V4's {@code CHECK} constraints
 * say too, but catching it here returns the error envelope instead of a
 * constraint failure.
 *
 * <p>{@code active} is a {@link Boolean} rather than a primitive so that omitting
 * it means "on sale", not "hidden"; the service applies that default.
 */
public record CreateLensRequest(
        @NotBlank(message = "Enter a lens name.") @Size(max = 160, message = "Lens name is too long.")
        String name,

        @NotNull(message = "Choose a lens type.") LensType type,

        @Size(max = 120, message = "Coating is too long.") String coating,

        @NotNull(message = "Enter a price.") @DecimalMin(value = "0.0", message = "Price cannot be negative.")
        BigDecimal price,

        @NotNull(message = "Enter a stock quantity.") @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQty,

        Boolean active) {}
