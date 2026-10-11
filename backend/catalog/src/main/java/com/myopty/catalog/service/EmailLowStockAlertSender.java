package com.myopty.catalog.service;

import com.myopty.catalog.config.LowStockAlertProperties;
import com.myopty.catalog.dto.AvailabilityItemResponse;
import com.myopty.catalog.model.StockStatus;
import java.util.List;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Sends a low-stock alert over the {@code MAIL_*} SMTP contract.
 *
 * <p>The message is rendered from the crossing items rather than stored: unlike
 * an order notification, the alert is ephemeral — there is no row that is the
 * record of what was said — so the body is built here from the same data the
 * report and the banner show. Only the subject is fixed, because the detail is
 * the body.
 *
 * <p>Delivery failures surface as a {@code MailException}; the caller decides
 * whether that is fatal. This class does not swallow them, because it cannot know
 * the deployment's posture on that.
 */
public class EmailLowStockAlertSender implements LowStockAlertSender {

    /** Constant because the item detail is already in the body. */
    static final String SUBJECT = "MyOpty low-stock alert";

    private final JavaMailSender mailSender;
    private final LowStockAlertProperties properties;

    public EmailLowStockAlertSender(JavaMailSender mailSender, LowStockAlertProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(List<AvailabilityItemResponse> items, String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(recipientEmail);
        message.setSubject(SUBJECT);
        message.setText(body(items));
        mailSender.send(message);
    }

    /**
     * The text the shop reads. Built here and not stored, so it can change without
     * a migration; the banner shows the same items, so the two cannot drift far.
     */
    static String body(List<AvailabilityItemResponse> items) {
        StringBuilder text = new StringBuilder("These items are low on stock after a recent order:\n");
        for (AvailabilityItemResponse item : items) {
            text.append("\n- ")
                    .append(item.itemType().equals("FRAME") ? "Frame" : "Lens")
                    .append(" ")
                    .append(item.name())
                    .append(": ")
                    .append(item.stockQty())
                    .append(" in stock, reorder at ")
                    .append(item.lowStockThreshold())
                    .append(" (")
                    .append(label(item.status()))
                    .append(")");
        }
        return text.toString();
    }

    private static String label(StockStatus status) {
        return status == StockStatus.OUT_OF_STOCK ? "out of stock" : "low stock";
    }
}
