package com.myopty.order.repository;

import com.myopty.order.model.Discount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Writes and reads {@code discount}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: the stories
 * built on it create a promotion, read the list back, and end one early, so those
 * are the methods declared. The active filter is applied in the service rather
 * than as a derived query, because "active" is a computed reading of a flag and
 * two dates, not a single column the query can name.
 */
public interface DiscountRepository extends Repository<Discount, Long> {

    Discount save(Discount discount);

    Optional<Discount> findById(Long id);

    /** Every promotion, newest first, which is also the promotion history. */
    List<Discount> findAllByOrderByIdDesc();

    /**
     * The promotions the shop has not ended, for pricing an item: the storefront
     * asks once per product, so this keeps ended rows out of the scan. The dates
     * are not part of the query because "running now" is a reading of today
     * against the window, which the caller applies.
     */
    List<Discount> findAllByActiveTrue();
}
