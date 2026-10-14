package com.myopty.billing.repository;

import com.myopty.billing.model.Invoice;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Writes and reads {@code invoice}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}. The batch read
 * ({@link #findAllByPaymentIdIn}) exists so a list of payments can be answered
 * with one query for their invoices instead of one per row — the shape that keeps
 * a page of the admin list at a fixed number of round trips.
 */
public interface InvoiceRepository extends Repository<Invoice, Long> {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    Optional<Invoice> findByPaymentId(Long paymentId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findAllByPaymentIdIn(Collection<Long> paymentIds);

    List<Invoice> findAllByOrderIdOrderByIdAsc(Long orderId);
}
