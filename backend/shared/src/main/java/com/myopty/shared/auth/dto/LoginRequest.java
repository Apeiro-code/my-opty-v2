package com.myopty.shared.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * The body of {@code POST /api/auth/login}.
 *
 * <p>Validation is deliberately thin: the endpoint's job is to answer "is this
 * email-and-password pair real", and a required-ness check is the only shape
 * question worth answering before the authentication manager is invoked.
 * Anything about account existence belongs to the response, not to parsing.
 */
public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
