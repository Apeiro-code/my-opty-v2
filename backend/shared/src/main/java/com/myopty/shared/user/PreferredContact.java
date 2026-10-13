package com.myopty.shared.user;

/**
 * How a customer prefers to be contacted. Mirrors the
 * {@code customer_profile.preferred_contact} CHECK in V1_1, which allows exactly
 * these three values; the enum makes an invalid one a compile error rather than a
 * constraint failure at the database.
 */
public enum PreferredContact {
    EMAIL,
    PHONE,
    SMS
}
