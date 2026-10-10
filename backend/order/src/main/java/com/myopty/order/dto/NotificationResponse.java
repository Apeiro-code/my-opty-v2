package com.myopty.order.dto;

import com.myopty.order.model.NotificationChannel;
import com.myopty.order.model.NotificationStatus;
import com.myopty.order.model.OrderNotification;
import java.time.LocalDateTime;

/**
 * One notification as the customer reads it back.
 *
 * <p>{@code orderNumber} is joined in for the display side: a list of messages
 * about {@code #42} is not something a customer can read. The order is this
 * module's own table, so resolving it adds no cross-module dependency.
 *
 * <p>{@code sentAt} is null while a row is {@code PENDING} or {@code FAILED}; the
 * global {@code non_null} inclusion drops it until a send succeeds.
 */
public record NotificationResponse(
        long id,
        long orderId,
        String orderNumber,
        NotificationChannel channel,
        String message,
        NotificationStatus status,
        LocalDateTime sentAt) {

    public static NotificationResponse from(OrderNotification notification, String orderNumber) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrderId(),
                orderNumber,
                notification.getChannel(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getSentAt());
    }
}
