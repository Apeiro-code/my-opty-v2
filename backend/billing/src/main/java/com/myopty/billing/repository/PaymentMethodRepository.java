package com.myopty.billing.repository;

import com.myopty.billing.model.PaymentMethod;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Reads {@code payment_method}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this story
 * only reads the methods the checkout offers and resolves one by its code, so it
 * does not inherit a dozen write methods the module does not use. Managing the
 * list of methods is an admin concern that will widen this deliberately.
 */
public interface PaymentMethodRepository extends Repository<PaymentMethod, Long> {

    Optional<PaymentMethod> findById(Long id);

    Optional<PaymentMethod> findByCode(String code);

    /** Batch lookup so a page of payments resolves their methods in one query. */
    List<PaymentMethod> findAllByIdIn(Collection<Long> ids);

    /** The active methods in the order the checkout shows them. */
    List<PaymentMethod> findAllByActiveTrueOrderBySortOrderAsc();

    List<PaymentMethod> findAllByOrderBySortOrderAsc();
}
