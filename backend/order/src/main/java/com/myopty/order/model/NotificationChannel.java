package com.myopty.order.model;

/**
 * How a notification travels to the customer.
 *
 * <p>Mirrors {@code order_notification.channel} (V102, default {@code EMAIL}).
 * An enum rather than a string so a second channel — SMS, push — is a compile-time
 * addition the moment the column's CHECK constraint is widened, not a name that
 * has to be remembered.
 */
public enum NotificationChannel {
    EMAIL
}
