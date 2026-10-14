package com.myopty.billing.model;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One refund issued against a payment: how much, why, and the gateway's handle
 * for the reversal.
 *
 * <p>A payment may be refunded more than once, so each reversal is its own row.
 * The rows are the history {@code payment.refundedAmount} is a running total of;
 * keeping both means a disagreement between the stored sum and the individual
 * refunds is something the data can be asked about, not hidden.
 *
 * <p>{@code refundedBy} is the admin who approved it (or null when none is
 * recorded), and {@code gatewayRefundId} is the gateway's reference, kept so a
 * support query can be traced back through the provider.
 */
@Table("payment_refund")
public class PaymentRefund {

    @Id
    private Long id;

    private Long paymentId;
    private BigDecimal amount;
    private String reason;
    private String gatewayRefundId;
    private Long refundedBy;

    @ReadOnlyProperty
    @Column("created_at")
    private java.time.LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getGatewayRefundId() {
        return gatewayRefundId;
    }

    public void setGatewayRefundId(String gatewayRefundId) {
        this.gatewayRefundId = gatewayRefundId;
    }

    public Long getRefundedBy() {
        return refundedBy;
    }

    public void setRefundedBy(Long refundedBy) {
        this.refundedBy = refundedBy;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
