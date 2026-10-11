package com.myopty.catalog.config;

import com.myopty.catalog.service.EmailLowStockAlertSender;
import com.myopty.catalog.service.LowStockAlertSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Wires the low-stock alert.
 *
 * <p>The {@link LowStockAlertSender} bean is the seam a second channel replaces:
 * the alerting service depends on the interface, so swapping SMTP for a shop
 * dashboard notification is this one method, not a hunt through callers.
 *
 * <p>The {@link JavaMailSender} arrives from {@code shared}'s
 * {@code spring-boot-starter-mail} and the same {@code MAIL_*} configuration order
 * already sends through, so the shop configures one mail contract for the whole
 * application.
 */
@Configuration
@EnableConfigurationProperties(LowStockAlertProperties.class)
public class LowStockAlertConfig {

    @Bean
    LowStockAlertSender lowStockAlertSender(JavaMailSender mailSender, LowStockAlertProperties properties) {
        return new EmailLowStockAlertSender(mailSender, properties);
    }
}
