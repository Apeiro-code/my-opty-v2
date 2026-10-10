package com.myopty.order.service;

import com.myopty.order.model.OrderNotification;

/**
 * The seam that turns a stored notification into one that actually leaves the
 * building.
 *
 * <p>The service writes the row and then asks a sender to deliver it; the sender
 * knows how, the service does not. Today there is one implementation, over SMTP,
 * but the workflow that calls this must not grow a second path when a customer's
 * preference is SMS — it is a new implementation of this interface.
 *
 * <p>A sender reports failure by throwing. The caller decides what that means: it
 * records {@code FAILED} and, if configured to, surfaces the error.
 */
public interface NotificationSender {

    /** Delivers one notification to {@code recipientEmail}. */
    void send(OrderNotification notification, String recipientEmail);
}
