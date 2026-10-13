package com.myopty.shared.user.dto;

import com.myopty.shared.user.PreferredContact;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The customer's editable profile fields.
 *
 * <p>Email is deliberately absent: it is the login identity, and changing it is a
 * verification story of its own, not an edit. Everything here is optional except
 * the name, which {@code app_user} requires.
 */
public record UpdateProfileRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 32) String phone,
        @Size(max = 500) String address,
        PreferredContact preferredContact) {}
