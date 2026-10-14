package com.myopty.billing.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * The invoice issued when a payment settles.
 *
 * <p>The money is copied here rather than recomputed from the order at read time:
 * a price change after the sale must not rewrite what the customer was charged, so
 * the invoice holds the {@code subtotal}, the {@code discountAmount}, the
 * {@code taxAmount} and the {@code total} as they were at issue. The four together
 * reconcile — {@code subtotal - discountAmount + taxAmount == total} — which is the
 * arithmetic this module is responsible for getting right.
 *
 * <p>{@code issuedAt} is set by the service rather than left to the column default,
 * because it is quoted on the rendered invoice and the service has already reasoned
 * about the moment of issue. {@code createdAt} is the database's and is read-only.
 */
@Table("invoice")
public class Invoice {

    @Id
    private Long id;

    private String invoiceNumber;
    private Long paymentId;
    private Long orderId;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String pdfObjectKey;
    private LocalDateTime issuedAt;

    @ReadOnlyProperty
    @Column("created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getPdfObjectKey() {
        return pdfObjectKey;
    }

    public void setPdfObjectKey(String pdfObjectKey) {
        this.pdfObjectKey = pdfObjectKey;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
