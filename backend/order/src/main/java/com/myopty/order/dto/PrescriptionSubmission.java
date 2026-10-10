package com.myopty.order.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The {@code prescription} part of the multipart submission: the optical values
 * exactly as they will be stored.
 *
 * <p>Every field is nullable and validated only when present, because a
 * prescription legitimately omits values — a single-vision order has no add
 * power, and a customer without astigmatism leaves cylinder and axis blank. What
 * is refused is a submission with no optical value at all, which is the one shape
 * the shop can do nothing with.
 *
 * <p>The pattern is deliberately loose: it accepts the signed, occasionally
 * two-decimal text an optician actually writes ({@code "+06.25"}, {@code "-1.75"},
 * {@code "180"}) and refuses free text, but it does not range-check, because the
 * values are transcribed verbatim and a wrong-but-plausible number is caught by
 * the shop's review, not by a regex. An empty string is allowed so a browser that
 * submits unfilled inputs as {@code ""} is not rejected; the service folds blank
 * to null.
 */
public record PrescriptionSubmission(
        @Pattern(
                regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$",
                message = "Sphere must be a signed number such as +06.25.")
        @Size(max = 16)
        String sphLeft,

        @Pattern(
                regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$",
                message = "Sphere must be a signed number such as +06.25.")
        @Size(max = 16)
        String sphRight,

        @Pattern(
                regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$",
                message = "Cylinder must be a signed number such as -1.75.")
        @Size(max = 16)
        String cylLeft,

        @Pattern(
                regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$",
                message = "Cylinder must be a signed number such as -1.75.")
        @Size(max = 16)
        String cylRight,

        @Pattern(regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$", message = "Axis must be a number such as 180.")
        @Size(max = 16)
        String axisLeft,

        @Pattern(regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$", message = "Axis must be a number such as 180.")
        @Size(max = 16)
        String axisRight,

        @Pattern(regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$", message = "Add power must be a number such as +2.00.")
        @Size(max = 16)
        String addPowerLeft,

        @Pattern(regexp = "^(?:[+-]?\\d{1,3}(?:\\.\\d{1,2})?)?$", message = "Add power must be a number such as +2.00.")
        @Size(max = 16)
        String addPowerRight,

        boolean progressive) {

    @AssertTrue(message = "Enter at least one optical value.")
    public boolean isAtLeastOneOpticalValuePresent() {
        return hasText(sphLeft)
                || hasText(sphRight)
                || hasText(cylLeft)
                || hasText(cylRight)
                || hasText(axisLeft)
                || hasText(axisRight)
                || hasText(addPowerLeft)
                || hasText(addPowerRight);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
