package com.myopty.billing.gateway;

import java.math.BigDecimal;

/**
 * The outcome of a {@link PaymentGateway} call: whether it succeeded, the
 * provider's handle for it, and — on refusal — why.
 *
 * <p>A failed result carries a reason written for a person, because it is what the
 * customer is told and what an admin sees in the payment's history. A successful
 * one carries the transaction id billing stores so a later refund can name the same
 * charge, and the amount actually settled (which for a partial capture could differ
 * from what was asked).
 *
 * @param successful whether the provider accepted the request
 * @param transactionId the provider's reference, present only on success
 * @param settledAmount the amount settled, present only on success
 * @param failureReason the human-readable reason, present only on failure
 */
public record GatewayResult(boolean successful, String transactionId, BigDecimal settledAmount, String failureReason) {

    public static GatewayResult success(String transactionId, BigDecimal settledAmount) {
        return new GatewayResult(true, transactionId, settledAmount, null);
    }

    public static GatewayResult failure(String failureReason) {
        return new GatewayResult(false, null, null, failureReason);
    }
}
