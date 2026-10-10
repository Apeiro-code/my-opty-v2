package com.myopty.order.service;

import com.myopty.order.config.NotificationProperties;
import com.myopty.order.model.OrderNotification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Sends an order notification over the {@code MAIL_*} SMTP contract.
 *
 * <p>The message body is the stored text sent verbatim: the row is what the shop
 * is accountable for having said, so re-rendering it here would let the email and
 * the record drift apart. Only the subject is added, because the row's purpose is
 * the body.
 *
 * <p>Delivery failures surface as a {@code MailException}; the caller decides
 * whether that is fatal. This class does not swallow them, because it cannot know
 * the deployment's posture on that.
 */
public class EmailNotificationSender implements NotificationSender {

    /** Constant because the order-specific detail is already in the body. */
    static final String SUBJECT = "MyOpty order update";

    private final JavaMailSender mailSender;
    private final NotificationProperties properties;

    public EmailNotificationSender(JavaMailSender mailSender, NotificationProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(OrderNotification notification, String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(recipientEmail);
        message.setSubject(SUBJECT);
        message.setText(notification.getMessage());
        mailSender.send(message);
    }
}
