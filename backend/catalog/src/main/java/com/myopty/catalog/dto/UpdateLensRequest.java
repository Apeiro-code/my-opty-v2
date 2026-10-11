package com.myopty.catalog.dto;

import com.myopty.catalog.model.LensType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * The body of {@code PUT /api/shop/lenses/{id}}: the lens as it should now be.
 *
 * <p>A full replacement rather than a patch — the edit form sends every field it
 * shows, so a request that omits one is a client mistake and is refused rather
 * than silently leaving the old value. It is a separate record from
 * {@link CreateLensRequest} because the two are free to diverge: an edit may one
 * day allow a field creation does not, or vice versa.
 *
 * <p>{@code categoryId} is the filing choice. It is nullable so a lens can stay
 * unfiled, which the V4 column allows.
 */
public record UpdateLensRequest(
        @NotBlank(message = "Enter a lens name.") @Size(max = 160, message = "Lens name is too long.")
        String name,

        @NotNull(message = "Choose a lens type.") LensType type,

        @Size(max = 120, message = "Coating is too long.") String coating,

        @NotNull(message = "Enter a price.") @DecimalMin(value = "0.0", message = "Price cannot be negative.")
        BigDecimal price,

        @NotNull(message = "Enter a stock quantity.") @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer stockQty,

        Boolean active,

        Long categoryId) {}
