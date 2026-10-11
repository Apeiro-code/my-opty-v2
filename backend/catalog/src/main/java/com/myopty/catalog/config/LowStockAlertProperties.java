package com.myopty.catalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How the catalog raises a low-stock alert.
 *
 * <p>The recipient is a shop setting rather than a user lookup: the alert goes to
 * the shop, and there is no role-to-mailbox mapping to resolve, so the address is
 * configured the way the SMTP credentials are. A blank address disables email
 * delivery while leaving the in-app banner working, which is what a developer
 * without a mail account wants; {@code enabled} is the switch that turns the
 * whole feature off.
 *
 * <p>The from address is separate from the SMTP credentials because it is the
 * visible sender, not a connection detail; it reuses {@code MAIL_FROM_ADDRESS},
 * the same value order's notifications send from.
 */
@ConfigurationProperties(prefix = "myopty.alerts")
public class LowStockAlertProperties {

    /** When false, no alert email is ever sent. The in-app banner is unaffected. */
    private boolean enabled = true;

    /** Where the alert is delivered. Blank means no email is sent. */
    private String recipientEmail = "";

    /** The visible From: address on every alert. */
    private String fromAddress = "no-reply@myopty.local";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
