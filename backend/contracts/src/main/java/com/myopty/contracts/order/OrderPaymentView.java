package com.myopty.contracts.order;

import java.math.BigDecimal;

/**
 * What {@code billing} needs to know about an order before it can charge for it:
 * whose it is, the human-readable number an invoice and receipt quote, the frame
 * and lens to price, and the order's workflow state.
 *
 * <p>It is deliberately not the order entity. {@code totalAmount} rides along
 * because an order may already carry a fixed price; when it is null billing
 * prices the frame and lens itself through {@link com.myopty.contracts.catalog.PriceQuery}.
 * {@code orderStatus} is the workflow state as its name, so billing can refuse to
 * charge a rejected order without importing {@code order}'s enum.
 *
 * @param orderId the order's primary key
 * @param customerId the owning customer, used for authorization and the payment row
 * @param orderNumber the customer-facing reference, quoted on the invoice
 * @param frameId the chosen frame, or null for a lenses-only order
 * @param lensId the chosen lens
 * @param quantity the number of units ordered
 * @param totalAmount a price already fixed on the order, or null to be computed
 * @param orderStatus the workflow state's name, e.g. {@code PENDING} or {@code REJECTED}
 */
public record OrderPaymentView(
        long orderId,
        long customerId,
        String orderNumber,
        Long frameId,
        Long lensId,
        int quantity,
        BigDecimal totalAmount,
        String orderStatus) {}
