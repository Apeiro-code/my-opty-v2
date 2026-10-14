package com.myopty.billing.service;

import com.myopty.billing.config.BillingProperties;
import com.myopty.billing.dto.PageResponse;
import com.myopty.billing.dto.PaymentConfirmationResponse;
import com.myopty.billing.dto.PaymentResponse;
import com.myopty.billing.dto.ProcessPaymentRequest;
import com.myopty.billing.exception.PaymentFailedException;
import com.myopty.billing.exception.PaymentNotAllowedException;
import com.myopty.billing.exception.ResourceNotFoundException;
import com.myopty.billing.gateway.GatewayCharge;
import com.myopty.billing.gateway.GatewayResult;
import com.myopty.billing.gateway.PaymentGateway;
import com.myopty.billing.model.Payment;
import com.myopty.billing.model.PaymentMethod;
import com.myopty.billing.model.PaymentStatus;
import com.myopty.billing.repository.PaymentMethodRepository;
import com.myopty.billing.repository.PaymentRepository;
import com.myopty.contracts.order.OrderPaymentView;
import com.myopty.contracts.order.OrderQuery;
import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.Role;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Takes a payment for an order: prices it, charges it through the gateway, records
 * the outcome and issues the receipt.
 *
 * <p>The amount is never the client's: the order is priced through
 * {@link OrderPricingService} and the total computed there is what is charged. The
 * gateway call happens outside any transaction — {@link PaymentRecorder} owns the
 * write — so a slow provider does not hold a database connection, and a connection
 * lost mid-charge does not roll back an attempt that the provider may have seen.
 *
 * <p>Cash on pickup is the one method that does not settle here: it is recorded
 * {@code PENDING} and the shop confirms it later, which is what
 * {@link #confirmCash} is for. A card or transfer that the gateway refuses is still
 * recorded, as {@code FAILED}, so the attempt is auditable and the order may be
 * retried with another method.
 */
@Service
public class PaymentService {

    /** The states that mean an order is already paid for and must not be charged again. */
    private static final EnumSet<PaymentStatus> ALREADY_PAID =
            EnumSet.of(PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED, PaymentStatus.PARTIALLY_REFUNDED);

    private static final String CASH_ON_PICKUP = "CASH_ON_PICKUP";

    private final OrderPricingService pricing;
    private final PaymentMethodService paymentMethods;
    private final PaymentMethodRepository methods;
    private final PaymentRepository payments;
    private final PaymentGateway gateway;
    private final PaymentRecorder recorder;
    private final InvoiceService invoiceService;
    private final ReceiptService receipts;
    private final PaymentViewAssembler assembler;
    private final OrderQuery orders;
    private final BillingProperties properties;

    public PaymentService(
            OrderPricingService pricing,
            PaymentMethodService paymentMethods,
            PaymentMethodRepository methods,
            PaymentRepository payments,
            PaymentGateway gateway,
            PaymentRecorder recorder,
            InvoiceService invoiceService,
            ReceiptService receipts,
            PaymentViewAssembler assembler,
            OrderQuery orders,
            BillingProperties properties) {
        this.pricing = pricing;
        this.paymentMethods = paymentMethods;
        this.methods = methods;
        this.payments = payments;
        this.gateway = gateway;
        this.recorder = recorder;
        this.invoiceService = invoiceService;
        this.receipts = receipts;
        this.assembler = assembler;
        this.orders = orders;
        this.properties = properties;
    }

    /**
     * Pays for an order by {@code request.paymentMethod}. The customer is the
     * session's, so a request cannot pay for someone else's order.
     */
    public PaymentResponse process(long customerId, ProcessPaymentRequest request) {
        OrderPricingService.OrderCharge charge = pricing.priceFor(request.orderId(), customerId);
        PaymentMethod method = paymentMethods.requireActiveByCode(request.paymentMethod());

        payments.findFirstByOrderIdAndStatusInOrderByIdDesc(request.orderId(), ALREADY_PAID)
                .ifPresent(existing -> {
                    throw new PaymentNotAllowedException("This order has already been paid for.");
                });

        Payment payment = newPayment(request.orderId(), customerId, method, charge.total());

        if (CASH_ON_PICKUP.equals(method.getCode())) {
            return assembler.toResponse(recorder.record(payment));
        }

        GatewayResult result = gateway.charge(new GatewayCharge(
                charge.orderId(),
                charge.orderNumber(),
                charge.total(),
                properties.getCurrency(),
                request.paymentDetails()));

        if (!result.successful()) {
            payment.setStatus(PaymentStatus.FAILED);
            recorder.record(payment);
            throw new PaymentFailedException(
                    result.failureReason() == null ? "The payment was declined." : result.failureReason());
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setGatewayTransactionId(result.transactionId());
        payment.setPaidAt(LocalDateTime.now());

        PaymentRecorder.Settlement settlement = recorder.settle(payment, charge);
        receipts.sendPaymentReceipt(settlement.payment(), settlement.invoice());
        return assembler.toResponse(settlement.payment());
    }

    /**
     * Confirms a cash-on-pickup payment that has been handed over, issuing its invoice
     * and receipt.
     *
     * <p>The invoice records the amount collected with no discount or tax split: that
     * breakdown was computed at checkout and is not stored on the payment, so inventing
     * one now from prices that may have moved would be worse than an honest flat total.
     */
    public PaymentResponse confirmCash(long customerId, long paymentId) {
        Payment payment = payments.findById(paymentId)
                .filter(found -> found.getCustomerId() == customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentNotAllowedException("This payment is not awaiting confirmation.");
        }
        PaymentMethod method = methods.findById(payment.getMethodId()).orElse(null);
        if (method == null || !CASH_ON_PICKUP.equals(method.getCode())) {
            throw new PaymentNotAllowedException("Only cash-on-pickup payments are confirmed this way.");
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setPaidAt(LocalDateTime.now());
        OrderPricingService.OrderCharge flat = new OrderPricingService.OrderCharge(
                payment.getOrderId(),
                orderNumber(payment.getOrderId()),
                payment.getAmount(),
                scale0(),
                scale0(),
                payment.getAmount());

        PaymentRecorder.Settlement settlement = recorder.settle(payment, flat);
        receipts.sendPaymentReceipt(settlement.payment(), settlement.invoice());
        return assembler.toResponse(settlement.payment());
    }

    /** The confirmation screen, keyed by the gateway's transaction id. */
    public PaymentConfirmationResponse readConfirmation(AppUser caller, String transactionId) {
        Payment payment = payments.findByGatewayTransactionId(transactionId)
                .filter(found -> caller.getRole() != Role.CUSTOMER
                        || found.getCustomerId().equals(caller.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));
        return assembler.toConfirmation(payment, orderNumber(payment.getOrderId()));
    }

    /** A customer's own payments, newest first. */
    public List<PaymentResponse> listOwn(long customerId) {
        return assembler.toResponses(payments.findAllByCustomerIdOrderByIdDesc(customerId));
    }

    /** The shop's filtered, paged payments. See {@link PageResponse}. */
    public PageResponse<PaymentResponse> listForAdmin(
            PaymentStatus status, LocalDate startDate, LocalDate endDate, int page, int limit) {
        int safePage = Math.max(page, 0);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        String statusName = status == null ? null : status.name();
        LocalDateTime startAt = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endAt = endDate == null ? null : endDate.plusDays(1).atStartOfDay();

        List<Payment> rows = payments.findForAdmin(statusName, startAt, endAt, safeLimit, safePage * safeLimit);
        long total = payments.countForAdmin(statusName, startAt, endAt);
        return PageResponse.of(assembler.toResponses(rows), safePage, safeLimit, total);
    }

    private Payment newPayment(long orderId, long customerId, PaymentMethod method, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCustomerId(customerId);
        payment.setMethodId(method.getId());
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRefundedAmount(scale0());
        return payment;
    }

    private String orderNumber(long orderId) {
        return orders.findForPayment(orderId).map(OrderPaymentView::orderNumber).orElse(null);
    }

    private static BigDecimal scale0() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
