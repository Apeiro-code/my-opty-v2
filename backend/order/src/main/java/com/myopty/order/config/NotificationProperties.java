package com.myopty.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How order notifications are sent.
 *
 * <p>These are the same {@code MAIL_*} values {@code spring.mail} already reads;
 * they are lifted into order's own namespace so the sending code depends on a
 * property it owns rather than reaching for another module's config. The from
 * address is separate from the SMTP credentials because it is the visible sender,
 * not a connection detail.
 *
 * <p>{@code failOnError} is off while a feature is being built and on in staging:
 * a customer who is never told their order is ready finds out at the counter, so a
 * production deployment wants the failure to be loud.
 */
@ConfigurationProperties(prefix = "myopty.notifications")
public class NotificationProperties {

    /** When true, a failed send is raised to the caller after the change is kept. */
    private boolean failOnError = false;

    /** The visible From: address on every order notification. */
    private String fromAddress = "no-reply@myopty.local";

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
