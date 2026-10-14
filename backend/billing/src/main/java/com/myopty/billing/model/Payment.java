package com.myopty.billing.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One attempt to pay for an order: how much, by which method, and what the
 * gateway did with it.
 *
 * <p>{@code amount} is the whole charge; {@code refundedAmount} is how much of it
 * has since gone back, and the difference is what a further refund may still take.
 * Keeping the original beside the running total, rather than netting them, is what
 * lets an invoice quote the price that was paid even after a partial refund.
 *
 * <p>The database fills {@code created_at} from its column default, so it is mapped
 * {@link ReadOnlyProperty} — read for an admin list, never written, so a save cannot
 * stamp the wrong time. {@code paidAt} is set by the service at capture because it
 * records when the gateway settled, not when the row was created.
 */
@Table("payment")
public class Payment {

    @Id
    private Long id;

    private Long orderId;
    private Long customerId;
    private Long methodId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private BigDecimal refundedAmount;
    private LocalDateTime paidAt;

    @ReadOnlyProperty
    @Column("created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getMethodId() {
        return methodId;
    }

    public void setMethodId(Long methodId) {
        this.methodId = methodId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
