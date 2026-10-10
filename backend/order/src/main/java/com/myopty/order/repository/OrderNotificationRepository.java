package com.myopty.order.repository;

import com.myopty.order.model.OrderNotification;
import java.util.List;
import org.springframework.data.repository.Repository;

/**
 * Writes and reads {@code order_notification}.
 *
 * <p>The customer-facing reads are scoped by {@code customerId} in the query, for
 * the reason {@link ProgressiveOrderRepository} gives: a session id in the query
 * cannot return someone else's notifications. The order-scoped read is the shop's
 * side and is unscoped by design, reached only through {@code /api/shop/**}.
 */
public interface OrderNotificationRepository extends Repository<OrderNotification, Long> {

    OrderNotification save(OrderNotification notification);

    List<OrderNotification> findAllByCustomerIdOrderByIdDesc(Long customerId);

    List<OrderNotification> findAllByCustomerIdAndOrderIdOrderByIdDesc(Long customerId, Long orderId);

    List<OrderNotification> findAllByOrderIdOrderByIdDesc(Long orderId);
}
