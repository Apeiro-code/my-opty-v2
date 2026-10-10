package com.myopty.order.dto;

import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.OrderType;
import com.myopty.order.model.ProgressiveOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * What the customer gets back for an order: the link they just made, read back
 * from storage, plus the workflow status.
 *
 * <p>{@code totalAmount} is null on a freshly created order — pricing belongs to
 * the billing module — so it is reported rather than hidden, to make it obvious
 * that no amount has been fixed yet.
 */
public record OrderResponse(
        long id,
        String orderNumber,
        OrderType orderType,
        OrderStatus status,
        long prescriptionId,
        Long frameId,
        long lensId,
        int quantity,
        BigDecimal totalAmount,
        LocalDateTime orderDate) {

    public static OrderResponse from(ProgressiveOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderType(),
                order.getStatus(),
                order.getPrescriptionId(),
                order.getFrameId(),
                order.getLensId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getOrderDate());
    }
}
