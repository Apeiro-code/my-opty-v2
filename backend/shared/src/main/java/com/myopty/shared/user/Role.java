package com.myopty.shared.user;

/**
 * The two halves of the door: a shop client who runs a store, and a customer who
 * shops at one.
 *
 * <p>The values match the {@code chk_app_user_role} check constraint in
 * {@code V1__shared_create_app_user.sql} — the database rejects anything else, so
 * this enum is the compiler-side mirror of a rule the schema already enforces.
 */
public enum Role {
    CUSTOMER,
    CLIENT
}
