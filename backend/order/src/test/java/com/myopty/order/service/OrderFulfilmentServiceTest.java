package com.myopty.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.order.dto.OrderResponse;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.OrderType;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.repository.ProgressiveOrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The shop moving an order down the production line: that a step may be skipped
 * but never reversed, that a pending order is refused, and that correcting a
 * receive date does not notify.
 *
 * <p>The repository is a hand-written fake so each starting status is one line,
 * and the transaction template runs inline because a unit test has no transaction
 * to join.
 */
class OrderFulfilmentServiceTest {

    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final OrderFulfilmentService service =
            new OrderFulfilmentService(orders, notifier, immediateTransactions());

    private static ProgressiveOrder order(long id, OrderStatus status) {
        ProgressiveOrder order = new ProgressiveOrder();
        order.setId(id);
        order.setOrderNumber("ORD-TEST-" + id);
        order.setCustomerId(7L);
        order.setPrescriptionId(30L);
        order.setLensId(12L);
        order.setOrderType(OrderType.PROGRESSIVE);
        order.setQuantity(1);
        order.setStatus(status);
        order.setOrderDate(LocalDateTime.of(2026, 10, 10, 9, 0));
        return order;
    }

    private static TransactionTemplate immediateTransactions() {
        return new TransactionTemplate(new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {}

            @Override
            public void rollback(TransactionStatus status) {}
        });
    }

    @Test
    void theActiveQueueHoldsEverythingBetweenApprovalAndDispatchOldestFirst() {
        orders.put(order(3L, OrderStatus.APPROVED));
        orders.put(order(1L, OrderStatus.PROCESSING));
        orders.put(order(2L, OrderStatus.READY));
        orders.put(order(4L, OrderStatus.PENDING));
        orders.put(order(5L, OrderStatus.DISPATCHED));

        assertThat(service.activeQueue()).extracting(OrderResponse::id).containsExactly(1L, 2L, 3L);
    }

    @Test
    void anApprovedOrderCanBeMarkedProcessingAndTheCustomerIsTold() {
        orders.put(order(4L, OrderStatus.APPROVED));

        OrderResponse response = service.advance(4L, OrderStatus.PROCESSING);

        assertThat(response.status()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(notifier.statuses).containsExactly(OrderStatus.PROCESSING);
    }

    @Test
    void aStepMayBeSkippedWhenTheFrameIsAlreadyInStock() {
        orders.put(order(4L, OrderStatus.APPROVED));

        OrderResponse response = service.advance(4L, OrderStatus.READY);

        assertThat(response.status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void aPendingOrderCannotBeProcessedBeforeItIsApproved() {
        orders.put(order(4L, OrderStatus.PENDING));

        assertThatThrownBy(() -> service.advance(4L, OrderStatus.PROCESSING)).isInstanceOf(InvalidStateException.class);
        assertThat(notifier.statuses).isEmpty();
    }

    @Test
    void anOrderCannotMoveBackwards() {
        orders.put(order(4L, OrderStatus.PROCESSING));

        assertThatThrownBy(() -> service.advance(4L, OrderStatus.APPROVED)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void aDispatchedOrderIsTerminal() {
        orders.put(order(4L, OrderStatus.DISPATCHED));

        assertThatThrownBy(() -> service.advance(4L, OrderStatus.READY)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void aRejectedOrderCannotMoveForward() {
        orders.put(order(4L, OrderStatus.REJECTED));

        assertThatThrownBy(() -> service.advance(4L, OrderStatus.PROCESSING)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void advancingAnOrderThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.advance(404L, OrderStatus.PROCESSING))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void theShopCanCorrectTheReceiveDateWithoutNotifying() {
        orders.put(order(4L, OrderStatus.APPROVED));

        OrderResponse response = service.updateReceiveDate(4L, LocalDate.of(2026, 11, 1));

        assertThat(response.receiveDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(notifier.statuses).isEmpty();
    }

    @Test
    void theShopCanWithdrawTheReceiveDate() {
        ProgressiveOrder approved = order(4L, OrderStatus.APPROVED);
        approved.setReceiveDate(LocalDate.of(2026, 11, 1));
        orders.put(approved);

        OrderResponse response = service.updateReceiveDate(4L, null);

        assertThat(response.receiveDate()).isNull();
    }

    @Test
    void aPendingOrderHasNoReceiveDateToCorrect() {
        orders.put(order(4L, OrderStatus.PENDING));

        assertThatThrownBy(() -> service.updateReceiveDate(4L, LocalDate.of(2026, 11, 1)))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void theReceiveDateCannotBeBeforeTheOrderWasPlaced() {
        orders.put(order(4L, OrderStatus.APPROVED));

        assertThatThrownBy(() -> service.updateReceiveDate(4L, LocalDate.of(2026, 1, 1)))
                .isInstanceOf(InvalidStateException.class);
    }

    private static final class RecordingNotifier implements OrderNotifier {

        private final List<OrderStatus> statuses = new ArrayList<>();

        @Override
        public void recordAndSend(ProgressiveOrder order, OrderStatus status) {
            statuses.add(status);
        }
    }

    private static final class FakeOrderRepository implements ProgressiveOrderRepository {

        private final Map<Long, ProgressiveOrder> byId = new HashMap<>();

        void put(ProgressiveOrder order) {
            byId.put(order.getId(), order);
        }

        @Override
        public ProgressiveOrder save(ProgressiveOrder order) {
            byId.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<ProgressiveOrder> findByIdAndCustomerId(Long id, Long customerId) {
            throw new UnsupportedOperationException("not used by the fulfilment story");
        }

        @Override
        public List<ProgressiveOrder> findAllByCustomerIdOrderByOrderDateDesc(Long customerId) {
            throw new UnsupportedOperationException("not used by the fulfilment story");
        }

        @Override
        public Optional<ProgressiveOrder> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<ProgressiveOrder> findAllByStatusOrderByOrderDateAsc(OrderStatus status) {
            throw new UnsupportedOperationException("not used by the fulfilment story");
        }

        @Override
        public List<ProgressiveOrder> findAllByStatusInOrderByOrderDateAsc(Collection<OrderStatus> statuses) {
            return byId.values().stream()
                    .filter(order -> statuses.contains(order.getStatus()))
                    .sorted(Comparator.comparing(ProgressiveOrder::getOrderDate))
                    .toList();
        }
    }
}
