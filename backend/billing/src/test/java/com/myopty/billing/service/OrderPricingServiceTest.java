package com.myopty.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.billing.config.BillingProperties;
import com.myopty.billing.exception.PaymentNotAllowedException;
import com.myopty.billing.exception.ResourceNotFoundException;
import com.myopty.contracts.catalog.PriceQuery;
import com.myopty.contracts.order.AppliedDiscount;
import com.myopty.contracts.order.DiscountQuery;
import com.myopty.contracts.order.OrderPaymentView;
import com.myopty.contracts.order.OrderQuery;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The pricing arithmetic is the part of billing a customer would notice getting
 * wrong, so it is pinned without a database: the four amounts, the discount taken
 * off each product, tax on the discounted figure, and the rules that refuse a bad
 * order before any money is involved.
 */
class OrderPricingServiceTest {

    private static final long CUSTOMER = 7L;
    private static final long ORDER = 1L;

    @Test
    void pricesAFrameAndLensByTheirCataloguePricesTimesTheQuantity() {
        OrderPricingService.OrderCharge charge = service(
                        order().build(), price("100.00"), price("50.00"), null, taxRate("0"))
                .priceFor(ORDER, CUSTOMER);

        assertThat(charge.subtotal()).isEqualByComparingTo("300.00");
        assertThat(charge.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(charge.taxAmount()).isEqualByComparingTo("0.00");
        assertThat(charge.total()).isEqualByComparingTo("300.00");
    }

    @Test
    void takesTheBestDiscountOffEachProductAndTaxesTheDiscountedTotal() {
        AppliedDiscount tenPercent = new AppliedDiscount(new BigDecimal("10"), "Ten off");

        OrderPricingService.OrderCharge charge = service(
                        order().build(), price("100.00"), price("50.00"), tenPercent, taxRate("0.10"))
                .priceFor(ORDER, CUSTOMER);

        // (100 + 50) * 2 = 300 gross; 10% off each unit gives 270 net; 10% tax on 270 is 27.
        assertThat(charge.subtotal()).isEqualByComparingTo("300.00");
        assertThat(charge.discountAmount()).isEqualByComparingTo("30.00");
        assertThat(charge.taxAmount()).isEqualByComparingTo("27.00");
        assertThat(charge.total()).isEqualByComparingTo("297.00");
    }

    @Test
    void aFixedTotalOnTheOrderIsTakenAsTheSettledAmount() {
        OrderPricingService.OrderCharge charge = service(
                        order().totalAmount(new BigDecimal("250.50")).build(),
                        price("100.00"),
                        price("50.00"),
                        new AppliedDiscount(new BigDecimal("10"), "Ten off"),
                        taxRate("0.10"))
                .priceFor(ORDER, CUSTOMER);

        assertThat(charge.subtotal()).isEqualByComparingTo("250.50");
        assertThat(charge.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(charge.taxAmount()).isEqualByComparingTo("0.00");
        assertThat(charge.total()).isEqualByComparingTo("250.50");
    }

    @Test
    void refusesARejectedOrder() {
        assertThatThrownBy(() -> service(
                                order().status("REJECTED").build(), price("100.00"), price("50.00"), null, taxRate("0"))
                        .priceFor(ORDER, CUSTOMER))
                .isInstanceOf(PaymentNotAllowedException.class);
    }

    @Test
    void refusesAnOrderBelongingToSomeoneElse() {
        assertThatThrownBy(() -> service(
                                id -> Optional.of(order().customerId(999L).build()),
                                price("100.00"),
                                price("50.00"),
                                null,
                                taxRate("0"))
                        .priceFor(ORDER, CUSTOMER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void refusesAMissingOrder() {
        assertThatThrownBy(() -> service(id -> Optional.empty(), price("100.00"), price("50.00"), null, taxRate("0"))
                        .priceFor(ORDER, CUSTOMER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void refusesWhenAProductNoLongerHasAPrice() {
        assertThatThrownBy(() -> service(order().build(), null, price("50.00"), null, taxRate("0"))
                        .priceFor(ORDER, CUSTOMER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static OrderPricingService service(
            OrderPaymentView view,
            BigDecimal framePrice,
            BigDecimal lensPrice,
            AppliedDiscount discount,
            BillingProperties properties) {
        return service(id -> Optional.ofNullable(view), framePrice, lensPrice, discount, properties);
    }

    private static OrderPricingService service(
            OrderQuery orders,
            BigDecimal framePrice,
            BigDecimal lensPrice,
            AppliedDiscount discount,
            BillingProperties properties) {
        PriceQuery prices = new PriceQuery() {
            @Override
            public Optional<BigDecimal> framePrice(long id) {
                return Optional.ofNullable(framePrice);
            }

            @Override
            public Optional<BigDecimal> lensPrice(long id) {
                return Optional.ofNullable(lensPrice);
            }
        };
        DiscountQuery discounts = (kind, id) -> Optional.ofNullable(discount);
        return new OrderPricingService(orders, prices, discounts, properties);
    }

    private static Builder order() {
        return new Builder();
    }

    private static BigDecimal price(String value) {
        return new BigDecimal(value);
    }

    private static BillingProperties taxRate(String rate) {
        BillingProperties properties = new BillingProperties();
        properties.setTaxRate(new BigDecimal(rate));
        return properties;
    }

    /** Names only the fields a test cares about; the rest are a plain pending order. */
    private static final class Builder {

        private long orderId = ORDER;
        private long customerId = CUSTOMER;
        private String orderNumber = "ORD-1";
        private Long frameId = 10L;
        private Long lensId = 20L;
        private int quantity = 2;
        private BigDecimal totalAmount;
        private String status = "PENDING";

        Builder customerId(long value) {
            this.customerId = value;
            return this;
        }

        Builder totalAmount(BigDecimal value) {
            this.totalAmount = value;
            return this;
        }

        Builder status(String value) {
            this.status = value;
            return this;
        }

        OrderPaymentView build() {
            return new OrderPaymentView(
                    orderId, customerId, orderNumber, frameId, lensId, quantity, totalAmount, status);
        }
    }
}
