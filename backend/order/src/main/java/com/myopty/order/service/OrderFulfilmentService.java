package com.myopty.order.service;

import com.myopty.order.dto.OrderResponse;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.repository.ProgressiveOrderRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The shop moving an approved order down the production line, and correcting the
 * receive date it quoted.
 *
 * <p>The line is {@code APPROVED → PROCESSING → READY → DISPATCHED}. A step may be
 * skipped — a frame already in stock can go straight to ready — but never taken
 * backwards, and a {@code PENDING} order cannot be processed before it is approved.
 * {@link OrderStatus#isForwardTransition} is that rule; the service only phrases
 * the refusal.
 *
 * <p>Each accepted move commits before the customer is notified, for the reason
 * {@link OrderReviewService} gives: a mail failure must not undo the move.
 * Correcting the receive date is not a status change, so it notifies no one.
 */
@Service
public class OrderFulfilmentService {

    /** The README's cap on queue endpoints: at most 100 rows, oldest first. */
    private static final int MAX_QUEUE_SIZE = 100;

    /** The statuses still in the shop's hands: everything between approve and dispatch. */
    private static final List<OrderStatus> ACTIVE_STATUSES =
            List.of(OrderStatus.APPROVED, OrderStatus.PROCESSING, OrderStatus.READY);

    private final ProgressiveOrderRepository orders;
    private final OrderNotifier notifications;
    private final TransactionTemplate transactionTemplate;

    public OrderFulfilmentService(
            ProgressiveOrderRepository orders, OrderNotifier notifications, TransactionTemplate transactionTemplate) {
        this.orders = orders;
        this.notifications = notifications;
        this.transactionTemplate = transactionTemplate;
    }

    /** Orders the shop still has to work on, oldest first. */
    @Transactional(readOnly = true)
    public List<OrderResponse> activeQueue() {
        return orders.findAllByStatusInOrderByOrderDateAsc(ACTIVE_STATUSES).stream()
                .limit(MAX_QUEUE_SIZE)
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse advance(long orderId, OrderStatus target) {
        ProgressiveOrder order = transactionTemplate.execute(state -> {
            ProgressiveOrder current =
                    orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
            if (current.getStatus() == OrderStatus.PENDING) {
                throw new InvalidStateException("The order must be approved before it can be processed.");
            }
            if (!OrderStatus.isForwardTransition(current.getStatus(), target)) {
                throw new InvalidStateException(
                        "The order cannot move from " + current.getStatus() + " to " + target + ".");
            }
            current.setStatus(target);
            return orders.save(current);
        });
        notifications.recordAndSend(order, target);
        return OrderResponse.from(order);
    }

    public OrderResponse updateReceiveDate(long orderId, LocalDate receiveDate) {
        ProgressiveOrder order = transactionTemplate.execute(state -> {
            ProgressiveOrder current =
                    orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
            if (current.getStatus().progressRank() < OrderStatus.APPROVED.progressRank()) {
                throw new InvalidStateException("The order has no receive date until it is approved.");
            }
            if (receiveDate != null
                    && receiveDate.isBefore(current.getOrderDate().toLocalDate())) {
                throw new InvalidStateException("The receive date cannot be before the order was placed.");
            }
            current.setReceiveDate(receiveDate);
            return orders.save(current);
        });
        return OrderResponse.from(order);
    }
}
