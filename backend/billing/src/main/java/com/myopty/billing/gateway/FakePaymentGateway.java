package com.myopty.billing.gateway;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

/**
 * The gateway a developer runs against: no network, no credentials, and a result
 * that is deterministic enough to test.
 *
 * <p>It settles immediately — there is no separate authorise-then-capture step to
 * wait on — so a successful charge comes back already captured, which is the same
 * shape a provider that supports immediate capture gives. It is the default
 * provider precisely so a fresh clone can take a payment with nothing configured.
 *
 * <p>The one bit of behaviour worth designing in is the failure path: a card that
 * must be declined for a test sends the token {@value #FAIL_TOKEN}. Without it every
 * test of the failure branch would need a real provider, and the failure branch is
 * exactly the one that is hardest to exercise by hand.
 */
public class FakePaymentGateway implements PaymentGateway {

    /** The token a caller sends to make the fake gateway refuse, for testing failure. */
    public static final String FAIL_TOKEN = "fail";

    private static final String TOKEN_DETAIL = "token";

    @Override
    public GatewayResult charge(GatewayCharge charge) {
        String token = charge.details().get(TOKEN_DETAIL);
        if (token == null || token.isBlank()) {
            return GatewayResult.failure("Payment details are required.");
        }
        if (FAIL_TOKEN.equalsIgnoreCase(token.trim())) {
            return GatewayResult.failure("The payment was declined.");
        }
        return GatewayResult.success(reference("FAKE"), charge.amount());
    }

    @Override
    public GatewayResult refund(String gatewayTransactionId, BigDecimal amount) {
        if (gatewayTransactionId == null || gatewayTransactionId.isBlank()) {
            return GatewayResult.failure("There is no gateway transaction to refund.");
        }
        return GatewayResult.success(reference("FAKE-REFUND"), amount);
    }

    private static String reference(String prefix) {
        return prefix + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
    }
}
