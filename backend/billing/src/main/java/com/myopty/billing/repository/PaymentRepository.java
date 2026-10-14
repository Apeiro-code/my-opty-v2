package com.myopty.billing.repository;

import com.myopty.billing.model.Payment;
import com.myopty.billing.model.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Writes and reads {@code payment}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this module
 * saves a payment, reads one back, and lists them for a customer or the shop.
 *
 * <p>The admin list is a single filtered query rather than a pile of derived
 * methods, because the filters are independent — any of status, an open date
 * range, or neither — and the combinations would otherwise be a method each. A
 * null parameter means "no filter", the same convention the storefront's frame
 * query uses. The date range is half-open ({@code >= start}, {@code < end}) so
 * consecutive days never double-count a boundary row.
 */
public interface PaymentRepository extends Repository<Payment, Long> {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    /** For the confirmation lookup, which is keyed by the gateway's transaction id. */
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    /**
     * The most recent payment for an order in any of {@code statuses}, for the
     * idempotency check: an order with a settled payment must not be charged again.
     */
    Optional<Payment> findFirstByOrderIdAndStatusInOrderByIdDesc(Long orderId, Collection<PaymentStatus> statuses);

    /** A customer's own payments, newest first, for the billing history page. */
    List<Payment> findAllByCustomerIdOrderByIdDesc(Long customerId);

    List<Payment> findAllByOrderIdOrderByIdDesc(Long orderId);

    /** The shop's filtered, newest-first page of payments. See the class comment. */
    @Query("""
            SELECT * FROM payment
            WHERE (:status IS NULL OR status = :status)
              AND (:startAt IS NULL OR created_at >= :startAt)
              AND (:endAt IS NULL OR created_at < :endAt)
            ORDER BY id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Payment> findForAdmin(
            @Param("status") String status,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /** How many rows {@link #findForAdmin} would return for the same filters. */
    @Query("""
            SELECT COUNT(*) FROM payment
            WHERE (:status IS NULL OR status = :status)
              AND (:startAt IS NULL OR created_at >= :startAt)
              AND (:endAt IS NULL OR created_at < :endAt)
            """)
    long countForAdmin(
            @Param("status") String status,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);
}
