package com.myopty.billing.repository;

import com.myopty.billing.model.PaymentRefund;
import java.util.List;
import org.springframework.data.repository.Repository;

/**
 * Writes and reads {@code payment_refund}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: the refund
 * service appends a row and, for an audit read, lists a payment's refunds oldest
 * first — the order they happened in.
 */
public interface PaymentRefundRepository extends Repository<PaymentRefund, Long> {

    PaymentRefund save(PaymentRefund refund);

    List<PaymentRefund> findAllByPaymentIdOrderByIdAsc(Long paymentId);
}
