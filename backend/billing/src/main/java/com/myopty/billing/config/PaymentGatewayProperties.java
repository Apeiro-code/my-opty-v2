package com.myopty.billing.config;

import java.util.Set;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The PAYMENT_GATEWAY_* half of the environment contract, bound so misconfiguration
 * fails at startup instead of at checkout.
 *
 * <p>Two values matter enough to be checked here rather than when a payment is attempted:
 * a provider this build does not know, and credentials that are missing for a provider
 * that needs them. Both name the exact {@code .env} variable to fix, because the person
 * reading the failure is holding their own file, not the source.
 *
 * <p>{@code sandbox} is only warned about, never refused: refusing {@code sandbox=false}
 * would make a deliberate live cutover impossible, but it must never happen by accident,
 * so the warning is written loudly enough to notice.
 */
@ConfigurationProperties(prefix = "myopty.payment")
public class PaymentGatewayProperties {

    /** Providers this build can talk to. Extend when a second gateway is integrated. */
    public static final Set<String> SUPPORTED_PROVIDERS = Set.of("fake", "payhere");

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayProperties.class);

    private String provider = "fake";
    private String merchantId = "";
    private String secretKey = "";
    private String apiVersion = "";
    private boolean sandbox = true;

    @PostConstruct
    void validate() {
        if (!SUPPORTED_PROVIDERS.contains(provider)) {
            throw new IllegalStateException(
                    "PAYMENT_GATEWAY_PROVIDER is '" + provider + "' but the supported providers are "
                            + String.join(", ", sortedProviders())
                            + ". Fix it in .env (see docs/DEPLOYMENT.md).");
        }

        if (!"fake".equals(provider) && (merchantId.isBlank() || secretKey.isBlank())) {
            throw new IllegalStateException(
                    "PAYMENT_GATEWAY_PROVIDER is '" + provider + "', which needs "
                            + "PAYMENT_GATEWAY_MERCHANT_ID and PAYMENT_GATEWAY_SECRET_KEY, but "
                            + (merchantId.isBlank() ? "PAYMENT_GATEWAY_MERCHANT_ID is empty" : "PAYMENT_GATEWAY_SECRET_KEY is empty")
                            + ". Copy the sandbox values into .env following docs/DEPLOYMENT.md, "
                            + "or set PAYMENT_GATEWAY_PROVIDER=fake to keep developing without an account.");
        }

        if (!sandbox) {
            log.warn("PAYMENT_GATEWAY_SANDBOX=false: provider '{}' is configured against LIVE "
                    + "credentials. Real money moves in this mode. Confirm that is intended "
                    + "before taking a payment.", provider);
        }
    }

    private String[] sortedProviders() {
        return SUPPORTED_PROVIDERS.stream().sorted().toArray(String[]::new);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }
}
