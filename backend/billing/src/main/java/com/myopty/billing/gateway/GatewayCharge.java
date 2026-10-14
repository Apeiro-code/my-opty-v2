package com.myopty.billing.gateway;

import java.math.BigDecimal;
import java.util.Map;

/**
 * What the payment service hands a gateway to charge for: the order being paid, the
 * amount billing computed, and whatever provider-specific fields the client sent.
 *
 * <p>The amount is passed in rather than left for the gateway to work out, so the
 * server-computed total is the one charged — the client never sends a price. The
 * {@code details} map is the provider's own vocabulary (a card token, a PayHere
 * return payload); billing does not interpret it, which is what keeps the payment
 * service free of provider specifics.
 *
 * @param orderId the order being paid, for correlation in the provider's records
 * @param orderNumber the human-readable reference to quote
 * @param amount the amount to charge, computed server-side
 * @param currency the ISO currency code
 * @param details provider-specific fields from the client; never null
 */
public record GatewayCharge(
        long orderId, String orderNumber, BigDecimal amount, String currency, Map<String, String> details) {

    public GatewayCharge {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
