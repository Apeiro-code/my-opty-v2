package com.myopty.billing.service;

import com.myopty.billing.config.BillingProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Sends receipts and refund confirmations over the {@code MAIL_*} SMTP contract that
 * {@code spring.mail} already reads.
 *
 * <p>It is a MIME message rather than a plain one only because the invoice travels
 * as an attachment; the body stays short text a person reads. The PDF is built from
 * bytes the caller already rendered, so this class never touches the database or the
 * renderer.
 *
 * <p>A delivery failure propagates as the mail framework's exception — the caller
 * decides whether that is loud — because the payment is already recorded either way.
 */
public class EmailReceiptSender implements ReceiptSender {

    private static final String ENCODING = "UTF-8";

    private final JavaMailSender mailSender;
    private final BillingProperties properties;

    public EmailReceiptSender(JavaMailSender mailSender, BillingProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendPaymentReceipt(ReceiptEmail receipt) {
        String subject = "Your MyOpty receipt " + receipt.invoiceNumber();
        String body = "Hello " + safeName(receipt) + ",\n\n"
                + "Your payment for order " + orderReference(receipt) + " has been received.\n"
                + "Total charged: " + receipt.total() + ".\n\n"
                + "Your invoice is attached.\n";
        send(receipt, subject, body);
    }

    @Override
    public void sendRefundConfirmation(ReceiptEmail receipt) {
        String subject = "A refund has been issued for " + receipt.invoiceNumber();
        String body = "Hello " + safeName(receipt) + ",\n\n"
                + "We have refunded " + receipt.refundedAmount() + " for order " + orderReference(receipt) + ".\n"
                + "The updated invoice is attached for your records.\n";
        send(receipt, subject, body);
    }

    private void send(ReceiptEmail receipt, String subject, String body) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODING);
            helper.setFrom(properties.getFromAddress());
            helper.setTo(receipt.recipient());
            helper.setSubject(subject);
            helper.setText(body);
            if (receipt.invoicePdf() != null && receipt.invoicePdf().length > 0) {
                helper.addAttachment(
                        "invoice-" + receipt.invoiceNumber() + ".pdf", new ByteArrayResource(receipt.invoicePdf()));
            }
        } catch (MessagingException exception) {
            throw new IllegalStateException("Could not compose the receipt email.", exception);
        }
        mailSender.send(message);
    }

    private static String safeName(ReceiptEmail receipt) {
        return receipt.customerName() == null || receipt.customerName().isBlank() ? "there" : receipt.customerName();
    }

    private static String orderReference(ReceiptEmail receipt) {
        return receipt.orderNumber() == null ? receipt.invoiceNumber() : receipt.orderNumber();
    }
}
