package com.myopty.shared.user;

/**
 * Where an account is in its lifecycle, mirroring {@code chk_app_user_status}.
 *
 * <p>Only {@link #ACTIVE} may authenticate. The other two exist so registration
 * (Epic 2) can create an account before the person has proven their email, and
 * so a compromised account can be stopped without deleting the rows that orders
 * and prescriptions point at.
 */
public enum AccountStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    DISABLED
}
