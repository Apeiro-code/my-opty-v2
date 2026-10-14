package com.myopty.billing.service;

import com.myopty.billing.dto.PaymentConfirmationResponse;
import com.myopty.billing.dto.PaymentResponse;
import com.myopty.billing.model.Invoice;
import com.myopty.billing.model.Payment;
import com.myopty.billing.model.PaymentMethod;
import com.myopty.billing.repository.InvoiceRepository;
import com.myopty.billing.repository.PaymentMethodRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Turns stored payments into the shapes the API returns, resolving the method name
 * and invoice number they refer to by id.
 *
 * <p>It exists so the three callers that return a payment — the process endpoint, the
 * customer history and the admin list — build the response the same way, and so the
 * batch path can resolve every method and invoice in one query instead of one per row.
 * Only the method <em>name</em> and the invoice <em>number</em> are added; the payment's
 * own fields come straight from the row.
 */
@Component
public class PaymentViewAssembler {

    private final PaymentMethodRepository methods;
    private final InvoiceRepository invoices;

    public PaymentViewAssembler(PaymentMethodRepository methods, InvoiceRepository invoices) {
        this.methods = methods;
        this.invoices = invoices;
    }

    public PaymentResponse toResponse(Payment payment) {
        PaymentMethod method = methods.findById(payment.getMethodId()).orElse(null);
        Invoice invoice = invoices.findByPaymentId(payment.getId()).orElse(null);
        return build(payment, method, invoice);
    }

    public List<PaymentResponse> toResponses(List<Payment> payments) {
        if (payments.isEmpty()) {
            return List.of();
        }
        Map<Long, PaymentMethod> methodById = methodIndex(payments);
        Map<Long, Invoice> invoiceByPayment = invoiceIndex(payments);
        return payments.stream()
                .map(payment ->
                        build(payment, methodById.get(payment.getMethodId()), invoiceByPayment.get(payment.getId())))
                .toList();
    }

    public PaymentConfirmationResponse toConfirmation(Payment payment, String orderNumber) {
        PaymentMethod method = methods.findById(payment.getMethodId()).orElse(null);
        Invoice invoice = invoices.findByPaymentId(payment.getId()).orElse(null);
        return new PaymentConfirmationResponse(
                payment.getGatewayTransactionId(),
                payment.getId(),
                payment.getOrderId(),
                orderNumber,
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getStatus().name(),
                method == null ? null : method.getCode(),
                method == null ? null : method.getDisplayName(),
                payment.getPaidAt(),
                invoice == null ? null : invoice.getId(),
                invoice == null ? null : invoice.getInvoiceNumber());
    }

    private PaymentResponse build(Payment payment, PaymentMethod method, Invoice invoice) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                method == null ? null : method.getCode(),
                method == null ? null : method.getDisplayName(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getStatus().name(),
                payment.getGatewayTransactionId(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                invoice == null ? null : invoice.getInvoiceNumber());
    }

    private Map<Long, PaymentMethod> methodIndex(List<Payment> payments) {
        Collection<Long> ids = payments.stream().map(Payment::getMethodId).collect(Collectors.toSet());
        return methods.findAllByIdIn(ids).stream().collect(Collectors.toMap(PaymentMethod::getId, Function.identity()));
    }

    private Map<Long, Invoice> invoiceIndex(List<Payment> payments) {
        Collection<Long> ids = payments.stream().map(Payment::getId).collect(Collectors.toSet());
        return invoices.findAllByPaymentIdIn(ids).stream()
                .collect(Collectors.toMap(Invoice::getPaymentId, Function.identity(), (first, second) -> first));
    }
}
