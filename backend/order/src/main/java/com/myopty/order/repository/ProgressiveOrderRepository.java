package com.myopty.order.repository;

import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.ProgressiveOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Writes and reads {@code progressive_order}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this story
 * creates an order and reads one back for its owner, the approval story adds the
 * unscoped read and the status queue, and the workflow and reporting stories will
 * widen it deliberately.
 *
 * <p>Ownership is part of the customer query, not a check the service remembers
 * to make: {@link #findByIdAndCustomerId} cannot return another customer's order,
 * which is the shape that keeps the mistake from being possible rather than merely
 * tested. {@link #findById} and the status query are the shop's side and are
 * unscoped by design; they are reached only through the role-gated
 * {@code /api/shop/**} endpoints.
 */
public interface ProgressiveOrderRepository extends Repository<ProgressiveOrder, Long> {

    ProgressiveOrder save(ProgressiveOrder order);

    Optional<ProgressiveOrder> findByIdAndCustomerId(Long id, Long customerId);

    Optional<ProgressiveOrder> findById(Long id);

    List<ProgressiveOrder> findAllByStatusOrderByOrderDateAsc(OrderStatus status);
}
