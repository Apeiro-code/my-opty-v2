package com.myopty.billing.service;

import com.myopty.billing.model.Invoice;
import com.myopty.billing.model.Payment;
import com.myopty.billing.model.PaymentRefund;
import com.myopty.billing.repository.InvoiceRepository;
import com.myopty.billing.repository.PaymentRefundRepository;
import com.myopty.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The transactional writes of the payment lifecycle, kept apart from
 * {@link PaymentService} so the gateway call can stay outside a transaction.
 *
 * <p>The gateway is a network call; holding a database transaction open across it
 * would tie up a connection for as long as the provider takes to answer, and would
 * roll a recorded attempt back if the connection dropped mid-call. So
 * {@link PaymentService} prices and charges first, and only then calls in here — in
 * one short transaction — to store the outcome. Because these are the only methods
 * that write, and each is {@code @Transactional}, the payment and its invoice are
 * committed together and a customer can never see a captured payment without its
 * invoice.
 */
@Service
public class PaymentRecorder {

    private final PaymentRepository payments;
    private final InvoiceRepository invoices;
    private final PaymentRefundRepository refunds;
    private final InvoiceService invoiceService;

    public PaymentRecorder(
            PaymentRepository payments,
            InvoiceRepository invoices,
            PaymentRefundRepository refunds,
            InvoiceService invoiceService) {
        this.payments = payments;
        this.invoices = invoices;
        this.refunds = refunds;
        this.invoiceService = invoiceService;
    }

    /** Stores a payment that has no invoice yet: a pending cash payment, or a failure. */
    @Transactional
    public Payment record(Payment payment) {
        return payments.save(payment);
    }

    /** Stores a settled payment and issues its invoice in the same transaction. */
    @Transactional
    public Settlement settle(Payment payment, OrderPricingService.OrderCharge charge) {
        Payment saved = payments.save(payment);
        Invoice invoice = invoiceService.issue(saved, charge);
        return new Settlement(saved, invoice);
    }

    /** Appends a refund and updates the payment's running refunded total and status. */
    @Transactional
    public Payment recordRefund(Payment payment, PaymentRefund refund) {
        refunds.save(refund);
        return payments.save(payment);
    }

    /** A settled payment and the invoice issued with it. */
    public record Settlement(Payment payment, Invoice invoice) {}
}
