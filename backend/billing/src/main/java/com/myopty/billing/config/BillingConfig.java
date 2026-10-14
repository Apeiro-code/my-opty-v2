package com.myopty.billing.config;

import com.myopty.billing.gateway.FakePaymentGateway;
import com.myopty.billing.gateway.PayHerePaymentGateway;
import com.myopty.billing.gateway.PaymentGateway;
import com.myopty.billing.service.EmailReceiptSender;
import com.myopty.billing.service.ReceiptSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Wires the two seams billing plugs a provider into: the payment gateway and the
 * receipt sender.
 *
 * <p>Both are chosen here rather than annotated as components so the choice is in
 * one place and the implementations stay plain classes a test can build by hand.
 * The gateway is picked from {@link PaymentGatewayProperties#getProvider()}, which
 * has already been validated at startup, so a provider this method does not name
 * cannot reach it; {@code fake} is the default and the only branch a fresh clone
 * takes.
 */
@Configuration
@EnableConfigurationProperties(BillingProperties.class)
public class BillingConfig {

    @Bean
    PaymentGateway paymentGateway(PaymentGatewayProperties gatewayProperties) {
        return switch (gatewayProperties.getProvider()) {
            case "payhere" -> new PayHerePaymentGateway(gatewayProperties);
            default -> new FakePaymentGateway();
        };
    }

    @Bean
    ReceiptSender receiptSender(JavaMailSender mailSender, BillingProperties properties) {
        return new EmailReceiptSender(mailSender, properties);
    }
}
