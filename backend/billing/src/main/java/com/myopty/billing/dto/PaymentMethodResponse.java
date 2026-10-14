package com.myopty.billing.dto;

import com.myopty.billing.model.PaymentMethod;

/**
 * A payment method the customer may choose at checkout.
 *
 * <p>Only the fields a checkout needs cross the wire: the code the payment request
 * names, and the label to show. {@code provider_config} is deliberately absent —
 * it is the gateway's credentials and must never leave the server.
 */
public record PaymentMethodResponse(long id, String code, String displayName) {

    public static PaymentMethodResponse from(PaymentMethod method) {
        return new PaymentMethodResponse(method.getId(), method.getCode(), method.getDisplayName());
    }
}
