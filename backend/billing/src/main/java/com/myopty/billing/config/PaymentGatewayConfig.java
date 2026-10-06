package com.myopty.billing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link PaymentGatewayProperties} with the context.
 *
 * <p>{@code @SpringBootApplication(scanBasePackages = "com.myopty")} already finds this
 * package, so the only thing missing was the {@code @EnableConfigurationProperties}
 * registration — a {@code @ConfigurationProperties} class is not a bean until something
 * asks for one.
 */
@Configuration
@EnableConfigurationProperties(PaymentGatewayProperties.class)
public class PaymentGatewayConfig {
}
