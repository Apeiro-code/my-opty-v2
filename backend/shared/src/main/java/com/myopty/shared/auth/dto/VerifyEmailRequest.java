package com.myopty.shared.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank String email, @NotBlank String code) {}
