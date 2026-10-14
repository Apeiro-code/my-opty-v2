package com.myopty.billing.gateway;

import java.math.BigDecimal;

/**
 * The seam between billing and whoever actually moves the money.
 *
 * <p>The service decides <em>that</em> a charge should happen and records the
 * outcome; the gateway knows how to talk to the provider. Keeping the provider
 * behind this interface is what lets the local build run against
 * {@link FakePaymentGateway} with no credentials at all, and lets a second
 * provider be a new implementation rather than a branch in the payment service.
 *
 * <p>A refusal is a normal answer — {@link GatewayResult#failure} — not an
 * exception: a declined card is an outcome the caller must record (as
 * {@code FAILED}) and answer, not a fault. Only a provider that cannot be reached
 * at all, or that answers nonsense, is raised as an exception by the caller's
 * handling of an error result.
 */
public interface PaymentGateway {

    /**
     * Authorises and, for a provider that settles immediately, captures the
     * charge. The token and any provider fields travel in
     * {@link GatewayCharge#details()}.
     */
    GatewayResult charge(GatewayCharge charge);

    /** Reverses all or part of a settled charge, identified by its transaction id. */
    GatewayResult refund(String gatewayTransactionId, BigDecimal amount);
}
