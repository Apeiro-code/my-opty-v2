package com.myopty.order.service;

import com.myopty.order.dto.OrderResponse;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.PrescriptionNotVerifiedException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
import com.myopty.order.repository.ProgressiveOrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The shop's side of an order: the approval queue and the two decisions that let
 * an order proceed to production or stop it.
 *
 * <p>The gate is the order's prescription. An order may only be approved once its
 * prescription is {@code VERIFIED}, which is what keeps unreviewed work out of
 * production. The prescription is read through this module's own repository — the
 * order already links it and both tables are owned here — so the check adds no
 * cross-module dependency.
 *
 * <p>Like prescription review, the decision is one-way: only a {@code PENDING}
 * order may be approved or rejected. A rejection stores its reason so the customer
 * is told why.
 */
@Service
public class OrderReviewService {

    /** The README's cap on queue endpoints: at most 100 rows, oldest first. */
    private static final int MAX_QUEUE_SIZE = 100;

    private final ProgressiveOrderRepository orders;
    private final PrescriptionRepository prescriptions;

    public OrderReviewService(ProgressiveOrderRepository orders, PrescriptionRepository prescriptions) {
        this.orders = orders;
        this.prescriptions = prescriptions;
    }

    /**
     * The orders waiting for a decision, oldest first. A null {@code status}
     * defaults to {@code PENDING}, the queue the shop works from.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> queue(OrderStatus status) {
        OrderStatus filter = status == null ? OrderStatus.PENDING : status;
        return orders.findAllByStatusOrderByOrderDateAsc(filter).stream()
                .limit(MAX_QUEUE_SIZE)
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse approve(long orderId) {
        ProgressiveOrder order = loadPending(orderId);
        requireVerifiedPrescription(order);
        order.setStatus(OrderStatus.APPROVED);
        return OrderResponse.from(orders.save(order));
    }

    @Transactional
    public OrderResponse reject(long orderId, String reason) {
        ProgressiveOrder order = loadPending(orderId);
        order.setStatus(OrderStatus.REJECTED);
        order.setRejectionReason(reason.trim());
        return OrderResponse.from(orders.save(order));
    }

    private ProgressiveOrder loadPending(long orderId) {
        ProgressiveOrder order =
                orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateException("This order has already been decided.");
        }
        return order;
    }

    private void requireVerifiedPrescription(ProgressiveOrder order) {
        Prescription prescription = prescriptions
                .findById(order.getPrescriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found."));
        if (prescription.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new PrescriptionNotVerifiedException(
                    "The prescription must be verified before the order can be approved.");
        }
    }
}
