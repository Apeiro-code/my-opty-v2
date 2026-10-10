package com.myopty.order.repository;

import com.myopty.order.model.ProgressiveOrder;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Writes and reads {@code progressive_order}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this story
 * creates an order and reads one back for its owner, so only those two methods
 * are declared. The workflow and reporting stories will widen it deliberately.
 *
 * <p>Ownership is part of the query, not a check the service remembers to make:
 * {@link #findByIdAndCustomerId} cannot return another customer's order, which is
 * the shape that keeps the mistake from being possible rather than merely tested.
 */
public interface ProgressiveOrderRepository extends Repository<ProgressiveOrder, Long> {

    ProgressiveOrder save(ProgressiveOrder order);

    Optional<ProgressiveOrder> findByIdAndCustomerId(Long id, Long customerId);
}
