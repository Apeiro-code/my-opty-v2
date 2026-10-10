package com.myopty.order.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * What the shop told a customer about an order, and whether it got through.
 *
 * <p>{@code message} is the exact text that was sent, copied into the row. V102
 * gives the reason: rewording a template must not change what a past notification
 * claims was said, so the text is a snapshot and never a lookup into a template.
 *
 * <p>The database fills {@code created_at} and {@code updated_at} from their
 * column defaults, so no fields exist for them and an insert never writes them.
 * {@code status} and {@code channel} are set by the service rather than left to
 * their column defaults, because a mapped field that is null is inserted as
 * {@code NULL} and would fail the {@code NOT NULL} before the default applied —
 * the same trap {@code ProgressiveOrder.orderDate} documents.
 */
@Table("order_notification")
public class OrderNotification {

    @Id
    private Long id;

    private Long orderId;
    private Long customerId;
    private NotificationChannel channel;
    private String message;
    private NotificationStatus status;
    private LocalDateTime sentAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
