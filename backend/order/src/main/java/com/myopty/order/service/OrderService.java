package com.myopty.order.service;

import com.myopty.order.dto.CreateOrderRequest;
import com.myopty.order.dto.OrderResponse;
import com.myopty.order.exception.PrescriptionRejectedException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
import com.myopty.order.repository.ProgressiveOrderRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates an order by linking a customer's prescription to the frame and lens it
 * is built with, and reads an order back for its owner.
 *
 * <p>The prescription is loaded with the customer id in the query, so an order
 * can only ever be built against the caller's own prescription. The frame and lens
 * are the one part this module cannot check itself: they live in the catalog
 * module, which this module must not read. They are persisted as given and the
 * database's foreign keys (V101) are the check that they exist — see
 * {@code OrderExceptionHandler}, which turns that constraint failure into a 400
 * rather than a 500.
 */
@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);

    private final ProgressiveOrderRepository orders;
    private final PrescriptionRepository prescriptions;

    public OrderService(ProgressiveOrderRepository orders, PrescriptionRepository prescriptions) {
        this.orders = orders;
        this.prescriptions = prescriptions;
    }

    @Transactional
    public OrderResponse create(long customerId, CreateOrderRequest request) {
        Prescription prescription = prescriptions
                .findByIdAndCustomerId(request.prescriptionId(), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found."));

        if (prescription.getVerificationStatus() == VerificationStatus.REJECTED) {
            throw new PrescriptionRejectedException("This prescription was rejected and cannot be ordered.");
        }

        ProgressiveOrder order = new ProgressiveOrder();
        order.setOrderNumber(newOrderNumber());
        order.setCustomerId(customerId);
        order.setPrescriptionId(prescription.getId());
        order.setFrameId(request.frameId());
        order.setLensId(request.lensId());
        order.setOrderType(request.orderType());
        order.setQuantity(request.quantity() == null ? 1 : request.quantity());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        return OrderResponse.from(orders.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse readOwn(long customerId, long orderId) {
        return orders.findByIdAndCustomerId(orderId, customerId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }

    /**
     * A human-readable reference such as {@code ORD-20261010-3F2A9C1B}.
     *
     * <p>The random suffix comes from a UUID rather than a per-day counter: a
     * counter needs a lock or a separate sequence table to be correct under
     * concurrency, and the unique index on {@code order_number} already turns the
     * astronomically unlikely collision into a refused insert rather than a
     * duplicate.
     */
    private static String newOrderNumber() {
        String suffix =
                UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return "ORD-" + LocalDateTime.now().format(ORDER_NUMBER_DATE) + "-" + suffix;
    }
}
