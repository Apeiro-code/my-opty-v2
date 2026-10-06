package com.myopty.billing.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The startup contract for payment configuration.
 *
 * <p>Each case is a developer's .env mistake: a provider nobody supports, credentials
 * missing for a provider that needs them, or the safe defaults. The assertions check that
 * the failure names the variable to fix — a validation message that does not point at the
 * file the developer is holding costs the team a support conversation every time.
 */
class PaymentGatewayPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(UserConfigurations.of(PaymentGatewayConfig.class));

    @Test
    void defaultsAreTheLocalFakeProvider() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            PaymentGatewayProperties properties = context.getBean(PaymentGatewayProperties.class);
            assertThat(properties.getProvider()).isEqualTo("fake");
            assertThat(properties.isSandbox()).isTrue();
        });
    }

    @Test
    void fakeProviderNeedsNoCredentials() {
        runner.withPropertyValues(
                        "myopty.payment.provider=fake",
                        "myopty.payment.merchant-id=",
                        "myopty.payment.secret-key=")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void payhereWithoutASecretFailsAndNamesTheVariable() {
        runner.withPropertyValues(
                        "myopty.payment.provider=payhere",
                        "myopty.payment.merchant-id=121000000")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("PAYMENT_GATEWAY_SECRET_KEY")
                            .hasStackTraceContaining("docs/DEPLOYMENT.md");
                });
    }

    @Test
    void payhereWithoutAMerchantIdFailsAndNamesTheVariable() {
        runner.withPropertyValues(
                        "myopty.payment.provider=payhere",
                        "myopty.payment.secret-key=abc123")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("PAYMENT_GATEWAY_MERCHANT_ID");
                });
    }

    @Test
    void payhereWithCredentialsStartsAndBinds() {
        runner.withPropertyValues(
                        "myopty.payment.provider=payhere",
                        "myopty.payment.merchant-id=121000000",
                        "myopty.payment.secret-key=sandbox-secret",
                        "myopty.payment.sandbox=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    PaymentGatewayProperties properties = context.getBean(PaymentGatewayProperties.class);
                    assertThat(properties.getMerchantId()).isEqualTo("121000000");
                    assertThat(properties.getSecretKey()).isEqualTo("sandbox-secret");
                    assertThat(properties.isSandbox()).isTrue();
                });
    }

    @Test
    void unknownProviderFailsAndListsWhatIsSupported() {
        runner.withPropertyValues("myopty.payment.provider=stripe")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("PAYMENT_GATEWAY_PROVIDER")
                            .hasStackTraceContaining("payhere");
                });
    }

    /**
     * Live mode is warned about, not refused: a deliberate cutover must stay possible, and
     * the refusal case belongs to whoever owns the live deployment, not to this class.
     */
    @Test
    void liveModeStartsWithAWarningRatherThanAFailure() {
        runner.withPropertyValues(
                        "myopty.payment.provider=payhere",
                        "myopty.payment.merchant-id=121000000",
                        "myopty.payment.secret-key=live-secret",
                        "myopty.payment.sandbox=false")
                .run(context -> assertThat(context).hasNotFailed());
    }
}
