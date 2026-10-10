package com.myopty.order.repository;

import com.myopty.order.model.Prescription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Reads and writes {@code prescription}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: each story
 * declares just the methods it needs. The submit story added {@link #save}; the
 * order story adds the owner-scoped reads, because an order has to link a
 * prescription the caller actually owns and a customer has to be able to pick one.
 *
 * <p>The customer id is part of every query rather than a check the caller makes:
 * {@link #findByIdAndCustomerId} cannot return someone else's prescription, and
 * {@link #findAllByCustomerIdOrderByIdDesc} cannot list them, which keeps the
 * cross-customer mistake from being possible instead of merely tested.
 */
public interface PrescriptionRepository extends Repository<Prescription, Long> {

    Prescription save(Prescription prescription);

    Optional<Prescription> findByIdAndCustomerId(Long id, Long customerId);

    List<Prescription> findAllByCustomerIdOrderByIdDesc(Long customerId);
}
