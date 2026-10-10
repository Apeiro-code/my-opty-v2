package com.myopty.order.config;

import com.myopty.order.service.EmailNotificationSender;
import com.myopty.order.service.NotificationSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Wires order notifications and the lab lead times.
 *
 * <p>The {@link NotificationSender} bean is the seam a second channel replaces:
 * the service depends on the interface, so swapping SMTP for an SMS gateway is
 * this one method, not a hunt through callers.
 *
 * <p>The {@link TransactionTemplate} exists because the status change and the
 * notification it announces must commit separately — the order is the source of
 * truth even if the mail server is down — so the services persist first through
 * this template and only then notify. It is a bean rather than a private field so
 * the transaction manager stays replaceable in a test.
 */
@Configuration
@EnableConfigurationProperties({LabProperties.class, NotificationProperties.class})
public class OrderNotificationConfig {

    @Bean
    NotificationSender notificationSender(JavaMailSender mailSender, NotificationProperties properties) {
        return new EmailNotificationSender(mailSender, properties);
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
