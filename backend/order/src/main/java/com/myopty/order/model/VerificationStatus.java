package com.myopty.order.model;

/**
 * Where a prescription stands in the shop's review.
 *
 * <p>The names are the literals the {@code chk_prescription_verification_status}
 * constraint accepts, and Spring Data JDBC maps an enum to its name, so the Java
 * value and the stored text cannot drift apart without the check constraint
 * refusing the row.
 *
 * <p>{@link #PENDING_REVIEW} is the only value this change can produce: verify and
 * reject arrive with the client review story, and the schema's default keeps a
 * row consistent even if a service forgets to set it.
 */
public enum VerificationStatus {
    PENDING_REVIEW,
    VERIFIED,
    REJECTED
}
