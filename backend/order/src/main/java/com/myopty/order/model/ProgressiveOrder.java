package com.myopty.order.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * An order: a prescription linked to the frame and lens it is made up with.
 *
 * <p>The frame and lens sit on the order rather than in an order-item table
 * because an optical order is one frame (optionally) and one lens, so V101 gives
 * the reason a line-item table would carry no information. {@code frameId} is
 * therefore null for a lenses-only order.
 *
 * <p>{@code orderType} is the selected lens type, snapshotted from the catalog at
 * creation; it is the routing key. {@code totalAmount} is left null here — pricing
 * is the billing module's job and needs the catalog's prices, which this module
 * deliberately does not read.
 *
 * <p>The database fills {@code created_at} and {@code updated_at} from their
 * column defaults, so no fields exist for them and an insert never writes them.
 * {@code orderDate} is set by the service instead of relying on its column
 * default, because a mapped field that is null is inserted as {@code NULL} and
 * would fail the column's {@code NOT NULL} before the default could apply.
 */
@Table("progressive_order")
public class ProgressiveOrder {

    @Id
    private Long id;

    private String orderNumber;
    private Long customerId;
    private Long prescriptionId;
    private Long frameId;
    private Long lensId;
    private OrderType orderType;
    private int quantity;
    private LocalDateTime orderDate;
    private LocalDate receiveDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String rejectionReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Long getFrameId() {
        return frameId;
    }

    public void setFrameId(Long frameId) {
        this.frameId = frameId;
    }

    public Long getLensId() {
        return lensId;
    }

    public void setLensId(Long lensId) {
        this.lensId = lensId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(LocalDate receiveDate) {
        this.receiveDate = receiveDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
