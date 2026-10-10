package com.myopty.order.service;

import com.myopty.order.config.NotificationProperties;
import com.myopty.order.dto.NotificationResponse;
import com.myopty.order.exception.NotificationDeliveryException;
import com.myopty.order.model.NotificationChannel;
import com.myopty.order.model.NotificationStatus;
import com.myopty.order.model.OrderNotification;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.repository.OrderNotificationRepository;
import com.myopty.order.repository.ProgressiveOrderRepository;
import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.AppUserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tells the customer what happened to their order, and keeps the receipt.
 *
 * <p>Sending is deliberately a separate transaction from the status change it
 * announces. The order is the source of truth: a mail server that is down must not
 * undo a shop's decision, so the change is committed first and this runs after it.
 * A send that fails leaves a {@code FAILED} row — visible history — and only
 * becomes an error to the caller when {@code fail-on-error} is on, which is what a
 * production deployment that cares about undelivered mail sets.
 *
 * <p>The recipient is resolved here from the order's customer id, after the change
 * is committed: an order's customer id is the only identity the notification row
 * needs, and {@code app_user} is shared, so reading the email breaks no module
 * boundary.
 */
@Service
public class NotificationService implements OrderNotifier {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final OrderNotificationRepository notifications;
    private final ProgressiveOrderRepository orders;
    private final AppUserRepository users;
    private final NotificationSender sender;
    private final NotificationProperties properties;

    public NotificationService(
            OrderNotificationRepository notifications,
            ProgressiveOrderRepository orders,
            AppUserRepository users,
            NotificationSender sender,
            NotificationProperties properties) {
        this.notifications = notifications;
        this.orders = orders;
        this.users = users;
        this.sender = sender;
        this.properties = properties;
    }

    /**
     * Records the move and tries to deliver it. Called after the status change has
     * committed, in its own transaction.
     */
    @Override
    @Transactional
    public void recordAndSend(ProgressiveOrder order, OrderStatus status) {
        OrderNotification notification = new OrderNotification();
        notification.setOrderId(order.getId());
        notification.setCustomerId(order.getCustomerId());
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setMessage(messageFor(order, status));
        notification.setStatus(NotificationStatus.PENDING);
        notification = notifications.save(notification);

        String email = recipientEmail(order.getCustomerId());
        if (email == null) {
            notification.setStatus(NotificationStatus.FAILED);
            notifications.save(notification);
            log.warn(
                    "No email address for customer {}; order {} notification not sent.",
                    order.getCustomerId(),
                    order.getId());
            failIfConfigured(order, null);
            return;
        }

        try {
            sender.send(notification, email);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notifications.save(notification);
        } catch (RuntimeException exception) {
            notification.setStatus(NotificationStatus.FAILED);
            notifications.save(notification);
            log.warn("Could not send order {} notification to {}: {}", order.getId(), email, exception.getMessage());
            failIfConfigured(order, exception);
        }
    }

    /** The caller's own notifications, newest first, optionally for one order. */
    @Transactional(readOnly = true)
    public List<NotificationResponse> listForCustomer(long customerId, Long orderId) {
        List<OrderNotification> rows = orderId == null
                ? notifications.findAllByCustomerIdOrderByIdDesc(customerId)
                : notifications.findAllByCustomerIdAndOrderIdOrderByIdDesc(customerId, orderId);
        return toResponses(rows);
    }

    /** One order's notification history, for the shop. Scoped by the endpoint's role. */
    @Transactional(readOnly = true)
    public List<NotificationResponse> listForOrder(long orderId) {
        return toResponses(notifications.findAllByOrderIdOrderByIdDesc(orderId));
    }

    private String recipientEmail(long customerId) {
        return users.findById(customerId)
                .map(AppUser::getEmail)
                .filter(email -> !email.isBlank())
                .orElse(null);
    }

    private void failIfConfigured(ProgressiveOrder order, RuntimeException cause) {
        if (properties.isFailOnError()) {
            throw new NotificationDeliveryException(
                    "The status change for order " + order.getId() + " was saved, but the customer was not notified.",
                    cause);
        }
    }

    private List<NotificationResponse> toResponses(List<OrderNotification> rows) {
        Map<Long, String> orderNumbers = new HashMap<>();
        return rows.stream()
                .map(row -> NotificationResponse.from(
                        row, orderNumbers.computeIfAbsent(row.getOrderId(), this::orderNumber)))
                .toList();
    }

    private String orderNumber(long orderId) {
        return orders.findById(orderId).map(ProgressiveOrder::getOrderNumber).orElse(null);
    }

    /**
     * The text stored with the row. Written here and never re-rendered, because
     * V102 makes the row, not a template, the record of what was said.
     */
    private static String messageFor(ProgressiveOrder order, OrderStatus status) {
        String number = order.getOrderNumber();
        return switch (status) {
            case APPROVED ->
                order.getReceiveDate() == null
                        ? "Good news — your order " + number + " has been approved."
                        : "Good news — your order " + number + " has been approved. Estimated ready date: "
                                + order.getReceiveDate() + ".";
            case REJECTED ->
                order.getRejectionReason() == null
                        ? "Your order " + number + " was not approved."
                        : "Your order " + number + " was not approved. Reason: " + order.getRejectionReason();
            case PROCESSING -> "Your order " + number + " is now being processed.";
            case READY -> "Your order " + number + " is ready for pickup at Flanet Opticals, Narammala.";
            case DISPATCHED -> "Your order " + number + " has been dispatched.";
            case PENDING ->
                throw new IllegalArgumentException("Pending is not a status the customer is notified about.");
        };
    }
}
