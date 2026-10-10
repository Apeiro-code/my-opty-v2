package com.myopty.order.model;

/**
 * Where a notification is in its own little lifecycle.
 *
 * <p>Mirrors the {@code chk_order_notification_status} constraint on
 * {@code order_notification} (V102). The row is written {@link #PENDING} the
 * moment the status change is recorded, then moved to {@link #SENT} or
 * {@link #FAILED} once the sender has been tried, so a failed email is a visible
 * row rather than only a log line that scrolls away.
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
