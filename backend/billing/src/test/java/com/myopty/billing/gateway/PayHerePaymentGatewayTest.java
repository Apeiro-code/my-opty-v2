package com.myopty.billing.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.myopty.billing.config.PaymentGatewayProperties;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * PayHere verifies by signature, so the one thing worth pinning is that a correctly
 * signed success payload is accepted and anything else is refused — a wrong signature,
 * or a status that is not "2".
 */
class PayHerePaymentGatewayTest {

    private static final String MERCHANT_ID = "merchant-1";
    private static final String SECRET_KEY = "secret-1";
    private static final String ORDER_ID = "ORD-1";
    private static final String AMOUNT = "100.00";
    private static final String CURRENCY = "LKR";

    private final PayHerePaymentGateway gateway = new PayHerePaymentGateway(properties());

    @Test
    void acceptsACorrectlySignedSuccessPayload() {
        GatewayResult result = charge(payload("2", signature()));

        assertThat(result.successful()).isTrue();
        assertThat(result.transactionId()).isEqualTo("PAY-1");
        assertThat(result.settledAmount()).isEqualByComparingTo(AMOUNT);
    }

    @Test
    void refusesAnUnsignedPayload() {
        GatewayResult result = charge(payload("2", "deadbeef"));

        assertThat(result.successful()).isFalse();
    }

    @Test
    void refusesAPayloadThatIsNotASuccessStatus() {
        GatewayResult result = charge(payload("-1", signature()));

        assertThat(result.successful()).isFalse();
    }

    @Test
    void refusesARefundBecauseThisBuildHasNoPayHereRefundApi() {
        GatewayResult result = gateway.refund("PAY-1", new BigDecimal(AMOUNT));

        assertThat(result.successful()).isFalse();
        assertThat(result.failureReason()).contains("dashboard");
    }

    private GatewayResult charge(Map<String, String> details) {
        return gateway.charge(new GatewayCharge(1L, ORDER_ID, new BigDecimal(AMOUNT), CURRENCY, details));
    }

    private static PaymentGatewayProperties properties() {
        PaymentGatewayProperties properties = new PaymentGatewayProperties();
        properties.setProvider("payhere");
        properties.setMerchantId(MERCHANT_ID);
        properties.setSecretKey(SECRET_KEY);
        return properties;
    }

    private static Map<String, String> payload(String statusCode, String signature) {
        Map<String, String> details = new HashMap<>();
        details.put("status_code", statusCode);
        details.put("order_id", ORDER_ID);
        details.put("payhere_amount", AMOUNT);
        details.put("payhere_currency", CURRENCY);
        details.put("merchant_id", MERCHANT_ID);
        details.put("payment_id", "PAY-1");
        details.put("md5sig", signature);
        return details;
    }

    private static String signature() {
        String secretHash = md5(SECRET_KEY).toUpperCase(Locale.ROOT);
        return md5(MERCHANT_ID + ORDER_ID + AMOUNT + CURRENCY + secretHash).toUpperCase(Locale.ROOT);
    }

    private static String md5(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
