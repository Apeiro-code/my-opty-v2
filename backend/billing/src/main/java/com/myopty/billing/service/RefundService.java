package com.myopty.billing.service;

import com.myopty.billing.dto.PaymentResponse;
import com.myopty.billing.dto.RefundRequest;
import com.myopty.billing.exception.InvalidPaymentException;
import com.myopty.billing.exception.PaymentGatewayException;
import com.myopty.billing.exception.RefundNotAllowedException;
import com.myopty.billing.exception.ResourceNotFoundException;
import com.myopty.billing.gateway.GatewayResult;
import com.myopty.billing.gateway.PaymentGateway;
import com.myopty.billing.model.Invoice;
import com.myopty.billing.model.Payment;
import com.myopty.billing.model.PaymentRefund;
import com.myopty.billing.model.PaymentStatus;
import com.myopty.billing.repository.PaymentRepository;
import com.myopty.shared.user.AppUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Returns money to a customer, all of a payment or part of it.
 *
 * <p>Only a settled payment can be refunded, and only up to what is still
 * outstanding: the running {@code refundedAmount} is the guard, so a second refund
 * cannot take back more than was ever charged. A refund against a payment that has
 * not settled, or that is already returned in full, is a state conflict; an amount
 * that is not positive, or that exceeds the balance, is a bad request.
 *
 * <p>Each refund appends a {@code payment_refund} row and updates the payment's total
 * and status in one transaction through {@link PaymentRecorder}. The status flips to
 * {@code REFUNDED} only when the running total reaches the amount charged, and stays
 * {@code PARTIALLY_REFUNDED} while any of it is still held. The confirmation email is
 * sent after the write, and never rolls it back.
 */
@Service
public class RefundService {

    private final PaymentRepository payments;
    private final PaymentGateway gateway;
    private final PaymentRecorder recorder;
    private final InvoiceService invoiceService;
    private final ReceiptService receipts;
    private final PaymentViewAssembler assembler;

    public RefundService(
            PaymentRepository payments,
            PaymentGateway gateway,
            PaymentRecorder recorder,
            InvoiceService invoiceService,
            ReceiptService receipts,
            PaymentViewAssembler assembler) {
        this.payments = payments;
        this.gateway = gateway;
        this.recorder = recorder;
        this.invoiceService = invoiceService;
        this.receipts = receipts;
        this.assembler = assembler;
    }

    /** Refunds {@code request.amount()} (or everything outstanding) of a payment. */
    public PaymentResponse refund(long paymentId, RefundRequest request, AppUser caller) {
        Payment payment =
                payments.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found."));

        if (!payment.getStatus().isRefundable()) {
            throw new RefundNotAllowedException("This payment cannot be refunded because it did not settle.");
        }

        BigDecimal alreadyRefunded = payment.getRefundedAmount() == null ? zero() : payment.getRefundedAmount();
        BigDecimal remaining = payment.getAmount().subtract(alreadyRefunded);
        if (remaining.signum() <= 0) {
            throw new RefundNotAllowedException("This payment has already been refunded in full.");
        }

        BigDecimal amount =
                request.amount() == null ? remaining : request.amount().setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() <= 0) {
            throw new InvalidPaymentException("The refund amount must be greater than zero.");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new InvalidPaymentException("The refund amount is more than the remaining refundable balance.");
        }

        GatewayResult result = gateway.refund(payment.getGatewayTransactionId(), amount);
        if (!result.successful()) {
            throw new PaymentGatewayException(
                    result.failureReason() == null ? "The refund could not be processed." : result.failureReason());
        }

        PaymentRefund refund = new PaymentRefund();
        refund.setPaymentId(payment.getId());
        refund.setAmount(amount);
        refund.setReason(request.reason());
        refund.setGatewayRefundId(result.transactionId());
        refund.setRefundedBy(caller == null ? null : caller.getId());

        BigDecimal totalRefunded = alreadyRefunded.add(amount);
        payment.setRefundedAmount(totalRefunded);
        payment.setStatus(
                totalRefunded.compareTo(payment.getAmount()) >= 0
                        ? PaymentStatus.REFUNDED
                        : PaymentStatus.PARTIALLY_REFUNDED);

        Payment saved = recorder.recordRefund(payment, refund);
        Invoice invoice = invoiceService.findByPaymentId(payment.getId()).orElse(null);
        if (invoice != null) {
            receipts.sendRefundConfirmation(saved, invoice, amount);
        }
        return assembler.toResponse(saved);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
