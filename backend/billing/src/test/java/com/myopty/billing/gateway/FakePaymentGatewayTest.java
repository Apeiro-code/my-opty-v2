package com.myopty.billing.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The fake gateway is the one every local build and test runs against, so its two
 * behaviours worth relying on — settle on a good token, refuse on a bad one — are
 * pinned here rather than left to the integration tests to discover.
 */
class FakePaymentGatewayTest {

    private static final BigDecimal AMOUNT = new BigDecimal("125.00");

    private final FakePaymentGateway gateway = new FakePaymentGateway();

    @Test
    void settlesImmediatelyWhenATokenIsPresent() {
        GatewayResult result = gateway.charge(charge(Map.of("token", "tok_visa")));

        assertThat(result.successful()).isTrue();
        assertThat(result.transactionId()).startsWith("FAKE-");
        assertThat(result.settledAmount()).isEqualByComparingTo(AMOUNT);
        assertThat(result.failureReason()).isNull();
    }

    @Test
    void refusesWhenNoDetailsWereSent() {
        GatewayResult result = gateway.charge(charge(Map.of()));

        assertThat(result.successful()).isFalse();
        assertThat(result.failureReason()).isNotBlank();
    }

    @Test
    void theDeclineTokenMakesItRefuse() {
        GatewayResult result = gateway.charge(charge(Map.of("token", FakePaymentGateway.FAIL_TOKEN)));

        assertThat(result.successful()).isFalse();
        assertThat(result.transactionId()).isNull();
    }

    @Test
    void refundsASettledTransaction() {
        GatewayResult result = gateway.refund("FAKE-123", AMOUNT);

        assertThat(result.successful()).isTrue();
        assertThat(result.transactionId()).startsWith("FAKE-REFUND-");
    }

    @Test
    void refusesToRefundWithoutATransaction() {
        assertThat(gateway.refund(null, AMOUNT).successful()).isFalse();
        assertThat(gateway.refund("  ", AMOUNT).successful()).isFalse();
    }

    private static GatewayCharge charge(Map<String, String> details) {
        return new GatewayCharge(1L, "ORD-1", AMOUNT, "LKR", details);
    }
}
