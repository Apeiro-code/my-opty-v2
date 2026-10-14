package com.myopty.billing.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The shop's billing settings: the tax rate applied to a computed total, the
 * currency every amount is quoted in, and how receipts are sent.
 *
 * <p>The tax rate is a single rate for the whole shop rather than a per-product
 * thing: this build has one jurisdiction, and per-line tax is a change that would
 * arrive with a second one, not a knob to add now. It defaults to zero so a fresh
 * clone charges exactly the catalogue price and the arithmetic stays obvious.
 *
 * <p>{@code failOnError} mirrors the order module's notification switch: off while
 * a feature is being built, on in staging so a receipt that never left is loud.
 * The from address is the visible sender and is deliberately separate from the
 * SMTP connection details, which Spring reads from its own {@code spring.mail}
 * namespace.
 */
@ConfigurationProperties(prefix = "myopty.billing")
public class BillingProperties {

    /** The fractional tax applied to the discounted subtotal, e.g. {@code 0.18} for 18%. */
    private BigDecimal taxRate = BigDecimal.ZERO;

    /** The ISO currency every amount on an invoice or payment is quoted in. */
    private String currency = "LKR";

    /** When true, a receipt that cannot be sent is raised to the caller. */
    private boolean failOnError = false;

    /** The visible From: address on every receipt. */
    private String fromAddress = "no-reply@myopty.local";

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
