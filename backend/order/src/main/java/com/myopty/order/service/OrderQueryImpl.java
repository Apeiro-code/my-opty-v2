package com.myopty.order.service;

import com.myopty.contracts.order.OrderPaymentView;
import com.myopty.contracts.order.OrderQuery;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.repository.ProgressiveOrderRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * The order module's side of {@link OrderQuery}: it hands billing the slice of an
 * order a payment needs without billing reading {@code progressive_order}.
 *
 * <p>It is a thin adapter over the order repository, so the meaning of an order's
 * fields stays defined in one place. The status crosses as its name, not as the
 * enum, which is what lets billing refuse a rejected order without importing this
 * module's type.
 */
@Service
public class OrderQueryImpl implements OrderQuery {

    private final ProgressiveOrderRepository orders;

    public OrderQueryImpl(ProgressiveOrderRepository orders) {
        this.orders = orders;
    }

    @Override
    public Optional<OrderPaymentView> findForPayment(long orderId) {
        return orders.findById(orderId).map(OrderQueryImpl::toView);
    }

    private static OrderPaymentView toView(ProgressiveOrder order) {
        return new OrderPaymentView(
                order.getId(),
                order.getCustomerId(),
                order.getOrderNumber(),
                order.getFrameId(),
                order.getLensId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus().name());
    }
}
