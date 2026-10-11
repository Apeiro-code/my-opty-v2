package com.myopty.order.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The body of {@code POST /api/shop/discounts} and {@code PUT
 * /api/shop/discounts/{id}}: the promotion the shop is setting up.
 *
 * <p>The same record serves create and update because a PUT replaces the whole
 * promotion and there is no field one carries and the other does not: the flag
 * that "end early" flips is not editable here, so a client can only end a
 * discount, never quietly reactivate one.
 *
 * <p>{@code percentage} is bounded to the range V103's {@code
 * chk_discount_percentage} enforces, so a bad value is refused with a message
 * rather than by a constraint failure. {@code targetItemIds} is optional: null or
 * empty means the promotion is sitewide. The one rule Bean Validation cannot
 * express — that {@code validTo} is not before {@code validFrom} — is checked by
 * the service before the row is written.
 */
public record DiscountRequest(
        @NotBlank(message = "Describe the promotion.") @Size(max = 500)
        String description,

        @NotNull(message = "Enter a discount percentage.")
        @DecimalMin(value = "0.01", message = "The percentage must be greater than zero.")
        @DecimalMax(value = "100.00", message = "The percentage cannot exceed 100.")
        BigDecimal percentage,

        List<
                        @NotNull(message = "A target id cannot be blank.")
                        @Positive(message = "A target id must be positive.") Long>
                targetItemIds,

        @NotNull(message = "Choose the date the promotion starts.")
        LocalDate validFrom,

        @NotNull(message = "Choose the date the promotion ends.")
        LocalDate validTo) {}
