package com.myopty.billing.service;

import com.myopty.billing.dto.InvoiceResponse;
import com.myopty.billing.exception.ResourceNotFoundException;
import com.myopty.billing.model.Invoice;
import com.myopty.billing.model.Payment;
import com.myopty.billing.repository.InvoiceRepository;
import com.myopty.billing.repository.PaymentRepository;
import com.myopty.contracts.order.OrderPaymentView;
import com.myopty.contracts.order.OrderQuery;
import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.AppUserRepository;
import com.myopty.shared.user.Role;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues and reads invoices: the durable record of what a customer was charged.
 *
 * <p>{@link #issue} is called by {@link PaymentRecorder} inside the transaction that
 * stores the payment, so an invoice never exists without the payment it belongs to.
 * The money is copied from the computed charge rather than recomputed, so the invoice
 * is a snapshot of the sale and a later price change cannot rewrite it.
 *
 * <p>Reads are ownership-checked: a customer may see only their own invoice, and a
 * missing one and someone else's answer the same 404 so neither can be told from the
 * other. The shop's roles see any invoice.
 */
@Service
public class InvoiceService {

    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);

    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final AppUserRepository users;
    private final OrderQuery orders;
    private final InvoicePdfRenderer renderer;

    public InvoiceService(
            InvoiceRepository invoices,
            PaymentRepository payments,
            AppUserRepository users,
            OrderQuery orders,
            InvoicePdfRenderer renderer) {
        this.invoices = invoices;
        this.payments = payments;
        this.users = users;
        this.orders = orders;
        this.renderer = renderer;
    }

    /** Issues the invoice for a just-saved payment. Joins the caller's transaction. */
    @Transactional
    public Invoice issue(Payment payment, OrderPricingService.OrderCharge charge) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(newInvoiceNumber());
        invoice.setPaymentId(payment.getId());
        invoice.setOrderId(payment.getOrderId());
        invoice.setSubtotal(charge.subtotal());
        invoice.setDiscountAmount(charge.discountAmount());
        invoice.setTaxAmount(charge.taxAmount());
        invoice.setTotal(charge.total());
        invoice.setIssuedAt(LocalDateTime.now());
        return invoices.save(invoice);
    }

    public Optional<Invoice> findByPaymentId(long paymentId) {
        return invoices.findByPaymentId(paymentId);
    }

    public Invoice requireForPayment(long paymentId) {
        return invoices.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found."));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse read(long invoiceId, AppUser caller) {
        return InvoiceResponse.from(requireOwned(invoiceId, caller));
    }

    @Transactional(readOnly = true)
    public byte[] renderPdf(long invoiceId, AppUser caller) {
        Invoice invoice = requireOwned(invoiceId, caller);
        Payment payment = payments.findById(invoice.getPaymentId()).orElse(null);
        String orderNumber = orders.findForPayment(invoice.getOrderId())
                .map(OrderPaymentView::orderNumber)
                .orElse(null);
        String customerName = customerName(payment);
        return renderer.render(invoice, payment, orderNumber, customerName);
    }

    private Invoice requireOwned(long invoiceId, AppUser caller) {
        Invoice invoice =
                invoices.findById(invoiceId).orElseThrow(() -> new ResourceNotFoundException("Invoice not found."));
        if (caller != null && caller.getRole() == Role.CUSTOMER) {
            long owner = payments.findById(invoice.getPaymentId())
                    .map(Payment::getCustomerId)
                    .orElse(-1L);
            if (owner != caller.getId()) {
                throw new ResourceNotFoundException("Invoice not found.");
            }
        }
        return invoice;
    }

    private String customerName(Payment payment) {
        if (payment == null) {
            return null;
        }
        return users.findById(payment.getCustomerId()).map(AppUser::getFullName).orElse(null);
    }

    /**
     * A reference such as {@code INV-20261014-9C1F2A7B}. The random suffix is a UUID
     * rather than a per-day counter for the same reason the order number uses one: a
     * counter needs a lock to stay correct under concurrency, and the unique index on
     * {@code invoice_number} already turns a collision into a refused insert.
     */
    private static String newInvoiceNumber() {
        String suffix =
                UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return "INV-" + LocalDateTime.now().format(NUMBER_DATE) + "-" + suffix;
    }
}
