package com.myopty.billing.gateway;

import com.myopty.billing.config.PaymentGatewayProperties;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;

/**
 * The PayHere adapter. PayHere settles a card on its own hosted page and returns
 * to us with a signed result, so this does not call an authorise API — it verifies
 * the {@code md5sig} on the return payload the client posts back and reads the
 * {@code status_code} from it, which is what turns PayHere's redirect flow into the
 * single {@link PaymentGateway#charge} call billing expects.
 *
 * <p>The signature is the one PayHere documents:
 * {@code MD5(merchant_id + order_id + payhere_amount + payhere_currency +
 * MD5(secret_key))}, upper-cased. Verifying it in constant time-ish is not the
 * point here — the secret never leaves the server and a mismatch only refuses the
 * charge — but comparing the whole string rather than a prefix is. A payload that
 * fails the check is refused with a reason, never trusted.
 *
 * <p>Refunds are issued from the PayHere dashboard: the classic integration has no
 * server refund endpoint this build calls, so {@link #refund} refuses with that
 * explanation rather than pretending to have moved money. A store that needs
 * programmatic refunds needs the PayHere refund API integrated here first.
 */
public class PayHerePaymentGateway implements PaymentGateway {

    private static final String SUCCESS_STATUS_CODE = "2";

    private final PaymentGatewayProperties properties;

    public PayHerePaymentGateway(PaymentGatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public GatewayResult charge(GatewayCharge charge) {
        Map<String, String> details = charge.details();
        String statusCode = details.get("status_code");
        if (!SUCCESS_STATUS_CODE.equals(statusCode)) {
            return GatewayResult.failure("The payment was not completed.");
        }

        String merchantId = valueOrDefault(details.get("merchant_id"), properties.getMerchantId());
        String orderId = valueOrDefault(details.get("order_id"), charge.orderNumber());
        String amountText =
                valueOrDefault(details.get("payhere_amount"), charge.amount().toPlainString());
        String currency = valueOrDefault(details.get("payhere_currency"), charge.currency());
        String signature = details.get("md5sig");

        String expected = signature(merchantId, orderId, amountText, currency, properties.getSecretKey());
        if (signature == null || !expected.equalsIgnoreCase(signature)) {
            return GatewayResult.failure("The payment could not be verified.");
        }

        String transactionId = valueOrDefault(details.get("payment_id"), orderId);
        return GatewayResult.success(transactionId, new BigDecimal(amountText));
    }

    @Override
    public GatewayResult refund(String gatewayTransactionId, BigDecimal amount) {
        return GatewayResult.failure(
                "Refunds for the PayHere provider are issued from the PayHere dashboard; no refund API is integrated.");
    }

    private static String signature(
            String merchantId, String orderId, String amount, String currency, String secretKey) {
        String secretHash = md5Hex(secretKey).toUpperCase(Locale.ROOT);
        return md5Hex(merchantId + orderId + amount + currency + secretHash).toUpperCase(Locale.ROOT);
    }

    private static String md5Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            // MD5 is part of every JRE; reaching here would mean a broken platform.
            throw new IllegalStateException("MD5 is not available.", exception);
        }
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
