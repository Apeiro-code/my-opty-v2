package com.myopty.billing.service;

import com.myopty.billing.config.BillingProperties;
import com.myopty.billing.exception.ReceiptDeliveryException;
import com.myopty.billing.model.Invoice;
import com.myopty.billing.model.Payment;
import com.myopty.contracts.order.OrderPaymentView;
import com.myopty.contracts.order.OrderQuery;
import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.AppUserRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Gathers a receipt's contents and asks the {@link ReceiptSender} to deliver it.
 *
 * <p>It is the one place that decides what a receipt says: the recipient from the
 * payment's customer, the order number from the order contract, and the attached PDF
 * from the renderer. A receipt is <em>best-effort</em>: the payment is already
 * recorded, so a send that fails is logged, and only raised when
 * {@code myopty.billing.fail-on-error} is on — the same switch, and the same
 * reasoning, as the order module's notifications.
 *
 * <p>A customer with no resolvable email is skipped rather than failed: there is
 * nothing to send to, and the payment stands.
 */
@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);

    private final AppUserRepository users;
    private final OrderQuery orders;
    private final InvoicePdfRenderer renderer;
    private final ReceiptSender sender;
    private final BillingProperties properties;

    public ReceiptService(
            AppUserRepository users,
            OrderQuery orders,
            InvoicePdfRenderer renderer,
            ReceiptSender sender,
            BillingProperties properties) {
        this.users = users;
        this.orders = orders;
        this.renderer = renderer;
        this.sender = sender;
        this.properties = properties;
    }

    /** Emails the receipt for a settled payment, with its invoice attached. */
    public void sendPaymentReceipt(Payment payment, Invoice invoice) {
        ReceiptEmail receipt = build(payment, invoice, BigDecimal.ZERO);
        if (receipt == null) {
            return;
        }
        deliver(() -> sender.sendPaymentReceipt(receipt), payment.getId());
    }

    /** Emails confirmation that {@code refundedNow} was returned against a payment. */
    public void sendRefundConfirmation(Payment payment, Invoice invoice, BigDecimal refundedNow) {
        ReceiptEmail receipt = build(payment, invoice, refundedNow);
        if (receipt == null) {
            return;
        }
        deliver(() -> sender.sendRefundConfirmation(receipt), payment.getId());
    }

    private ReceiptEmail build(Payment payment, Invoice invoice, BigDecimal refundedNow) {
        AppUser customer = users.findById(payment.getCustomerId()).orElse(null);
        if (customer == null
                || customer.getEmail() == null
                || customer.getEmail().isBlank()) {
            log.warn(
                    "Skipping receipt for payment {}: customer {} has no email.",
                    payment.getId(),
                    payment.getCustomerId());
            return null;
        }
        String orderNumber = orders.findForPayment(payment.getOrderId())
                .map(OrderPaymentView::orderNumber)
                .orElse(null);
        byte[] pdf = renderer.render(invoice, payment, orderNumber, customer.getFullName());
        return new ReceiptEmail(
                customer.getEmail(),
                customer.getFullName(),
                orderNumber,
                invoice.getInvoiceNumber(),
                payment.getAmount(),
                refundedNow,
                pdf);
    }

    private void deliver(Runnable send, long paymentId) {
        try {
            send.run();
        } catch (RuntimeException exception) {
            if (properties.isFailOnError()) {
                throw new ReceiptDeliveryException(
                        "The receipt for payment " + paymentId + " could not be sent.", exception);
            }
            log.warn("Receipt for payment {} could not be sent: {}", paymentId, exception.getMessage());
        }
    }
}
