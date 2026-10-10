package com.myopty.order.service;

import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.ProgressiveOrder;

/**
 * The mutation services' view of notification: emit one, do not worry how.
 *
 * <p>{@link OrderReviewService} and {@link OrderFulfilmentService} change an
 * order's status and must tell the customer. They should not know whether the
 * telling is stored, emailed, queued or mocked, so they depend on this one method
 * rather than on {@link NotificationService} with its read side. The interface
 * keeps the seam that {@link NotificationSender} provides on the wire and repeats
 * it at the call site.
 */
public interface OrderNotifier {

    /** Records a status change for the customer and attempts to deliver it. */
    void recordAndSend(ProgressiveOrder order, OrderStatus status);
}
