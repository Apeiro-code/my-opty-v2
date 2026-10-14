package com.myopty.billing.service;

import com.myopty.billing.config.BillingProperties;
import com.myopty.billing.exception.PaymentNotAllowedException;
import com.myopty.billing.exception.ResourceNotFoundException;
import com.myopty.contracts.catalog.PriceQuery;
import com.myopty.contracts.order.AppliedDiscount;
import com.myopty.contracts.order.DiscountQuery;
import com.myopty.contracts.order.ItemKind;
import com.myopty.contracts.order.OrderPaymentView;
import com.myopty.contracts.order.OrderQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Works out what an order costs, server-side, from the order's own contents.
 *
 * <p>The client never sends a price: this is reachable only after billing has
 * loaded the order through {@link OrderQuery} and confirmed the caller owns it, so
 * the figure charged is one the server derived. It reads catalogue prices through
 * {@link PriceQuery} and promotions through {@link DiscountQuery}, both contracts,
 * so it never touches another module's tables.
 *
 * <p>An order that already carries a fixed {@code totalAmount} is priced at that
 * figure with no discount or tax added — the amount is taken as the settled price,
 * not a subtotal. Otherwise the subtotal is the frame plus the lens times the
 * quantity, the best promotion is taken off each product, tax is applied to the
 * discounted figure, and the four amounts are rounded once, at the end, so the
 * invoice reconciles to the cent.
 *
 * <p>Every money value is HALF_UP to two decimals, the same rounding an invoice is
 * printed at; doing it once at the end rather than per step is what keeps
 * {@code subtotal - discount + tax == total} exact.
 */
@Service
public class OrderPricingService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final OrderQuery orders;
    private final PriceQuery prices;
    private final DiscountQuery discounts;
    private final BillingProperties properties;

    public OrderPricingService(
            OrderQuery orders, PriceQuery prices, DiscountQuery discounts, BillingProperties properties) {
        this.orders = orders;
        this.prices = prices;
        this.discounts = discounts;
        this.properties = properties;
    }

    /**
     * The charge for {@code orderId}, as its owner sees it.
     *
     * @throws ResourceNotFoundException when the order is missing or belongs to
     *     someone else — the two are one answer so ownership cannot be probed
     * @throws PaymentNotAllowedException when the order is rejected and must not be paid
     */
    public OrderCharge priceFor(long orderId, long customerId) {
        OrderPaymentView order = orders.findForPayment(orderId)
                .filter(view -> view.customerId() == customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        if ("REJECTED".equals(order.orderStatus())) {
            throw new PaymentNotAllowedException("This order was rejected and cannot be paid for.");
        }

        if (order.totalAmount() != null) {
            BigDecimal total = money(order.totalAmount());
            return new OrderCharge(order.orderId(), order.orderNumber(), total, zero(), zero(), total);
        }

        int quantity = Math.max(order.quantity(), 1);
        BigDecimal framePrice = order.frameId() == null ? zero() : framePrice(order.frameId());
        BigDecimal lensPrice = lensPrice(order.lensId());

        BigDecimal grossUnit = framePrice.add(lensPrice);
        BigDecimal netUnit = discounted(order.frameId(), ItemKind.FRAME, framePrice)
                .add(discounted(order.lensId(), ItemKind.LENS, lensPrice));

        BigDecimal subtotal = money(grossUnit.multiply(BigDecimal.valueOf(quantity)));
        BigDecimal net = money(netUnit.multiply(BigDecimal.valueOf(quantity)));
        BigDecimal discountAmount = money(subtotal.subtract(net));
        BigDecimal taxAmount = money(net.multiply(properties.getTaxRate()));
        BigDecimal total = net.add(taxAmount);

        return new OrderCharge(order.orderId(), order.orderNumber(), subtotal, discountAmount, taxAmount, total);
    }

    private BigDecimal framePrice(long frameId) {
        return prices.framePrice(frameId)
                .orElseThrow(() -> new ResourceNotFoundException("The frame on this order is no longer available."));
    }

    private BigDecimal lensPrice(long lensId) {
        return prices.lensPrice(lensId)
                .orElseThrow(() -> new ResourceNotFoundException("The lens on this order is no longer available."));
    }

    /** Applies the best promotion for a product, if it has one, to its unit price. */
    private BigDecimal discounted(Long itemId, ItemKind kind, BigDecimal basePrice) {
        if (itemId == null) {
            return basePrice;
        }
        Optional<AppliedDiscount> discount = discounts.bestDiscountFor(kind, itemId);
        if (discount.isEmpty()) {
            return basePrice;
        }
        BigDecimal remainingPercent = HUNDRED.subtract(discount.get().percentage());
        return money(basePrice.multiply(remainingPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP));
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * What an order costs, broken down the way an invoice prints it:
     * {@code subtotal - discountAmount + taxAmount == total}.
     *
     * <p>The currency is not carried here; it is the shop's single setting and lives
     * in {@link BillingProperties}, not on each charge.
     */
    public record OrderCharge(
            long orderId,
            String orderNumber,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal total) {}
}
