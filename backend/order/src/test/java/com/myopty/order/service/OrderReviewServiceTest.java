package com.myopty.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.order.config.LabProperties;
import com.myopty.order.dto.OrderResponse;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.PrescriptionNotVerifiedException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.OrderType;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
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
 * The shop's order approval rules, without a database: that an order may only be
 * approved against a verified prescription, that a decision is one-way, that
 * approval quotes a receive date, and what the default queue holds.
 *
 * <p>Both repositories are hand-written fakes because the rule under test is the
 * gate between an order and its linked prescription, which a fake makes explicit
 * instead of hiding in a database fixture. The transaction template runs each
 * callback inline — there is nothing to commit to in a unit test — and the notifier
 * is a recorder, so "the customer was told" is an assertion, not a side effect.
 */
class OrderReviewServiceTest {

    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final FakePrescriptionRepository prescriptions = new FakePrescriptionRepository();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final OrderReviewService service =
            new OrderReviewService(orders, prescriptions, new LabProperties(), notifier, immediateTransactions());

    private static ProgressiveOrder order(long id, long prescriptionId, OrderStatus status) {
        ProgressiveOrder order = new ProgressiveOrder();
        order.setId(id);
        order.setOrderNumber("ORD-TEST-" + id);
        order.setCustomerId(7L);
        order.setPrescriptionId(prescriptionId);
        order.setLensId(12L);
        order.setOrderType(OrderType.PROGRESSIVE);
        order.setQuantity(1);
        order.setStatus(status);
        order.setOrderDate(LocalDateTime.of(2026, 10, 10, 9, 0));
        return order;
    }

    private static Prescription prescription(long id, VerificationStatus status) {
        Prescription prescription = new Prescription();
        prescription.setId(id);
        prescription.setVerificationStatus(status);
        return prescription;
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
    void approvingAPendingOrderWithAVerifiedPrescriptionMovesItToApproved() {
        orders.put(order(4L, 30L, OrderStatus.PENDING));
        prescriptions.put(prescription(30L, VerificationStatus.VERIFIED));

        OrderResponse response = service.approve(4L);

        assertThat(response.status()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    void approvingQuotesAReceiveDateFromTheLabLeadTimeAndTellsTheCustomer() {
        orders.put(order(4L, 30L, OrderStatus.PENDING));
        prescriptions.put(prescription(30L, VerificationStatus.VERIFIED));

        OrderResponse response = service.approve(4L);

        assertThat(response.receiveDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(notifier.statuses).containsExactly(OrderStatus.APPROVED);
    }

    @Test
    void approvingIsRefusedWhileThePrescriptionIsNotVerified() {
        orders.put(order(4L, 30L, OrderStatus.PENDING));
        prescriptions.put(prescription(30L, VerificationStatus.PENDING_REVIEW));

        assertThatThrownBy(() -> service.approve(4L)).isInstanceOf(PrescriptionNotVerifiedException.class);
        assertThat(orders.saved).isNull();
        assertThat(notifier.statuses).isEmpty();
    }

    @Test
    void rejectingAPendingOrderStoresTheReasonAndTellsTheCustomer() {
        orders.put(order(4L, 30L, OrderStatus.PENDING));

        OrderResponse response = service.reject(4L, "  Lens out of stock.  ");

        assertThat(response.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Lens out of stock.");
        assertThat(orders.saved.getRejectionReason()).isEqualTo("Lens out of stock.");
        assertThat(notifier.statuses).containsExactly(OrderStatus.REJECTED);
    }

    @Test
    void aRejectedOrderCannotBeApprovedAfterwards() {
        orders.put(order(4L, 30L, OrderStatus.REJECTED));
        prescriptions.put(prescription(30L, VerificationStatus.VERIFIED));

        assertThatThrownBy(() -> service.approve(4L)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void anApprovedOrderCannotBeDecidedAgain() {
        orders.put(order(4L, 30L, OrderStatus.APPROVED));

        assertThatThrownBy(() -> service.reject(4L, "changed my mind")).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void decidingAnOrderThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.approve(404L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void theQueueDefaultsToPendingOldestFirst() {
        orders.put(order(3L, 30L, OrderStatus.PENDING));
        orders.put(order(1L, 30L, OrderStatus.APPROVED));
        orders.put(order(2L, 30L, OrderStatus.PENDING));

        assertThat(service.queue(null)).extracting(OrderResponse::id).containsExactly(2L, 3L);
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
        private ProgressiveOrder saved;

        void put(ProgressiveOrder order) {
            byId.put(order.getId(), order);
        }

        @Override
        public ProgressiveOrder save(ProgressiveOrder order) {
            this.saved = order;
            byId.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<ProgressiveOrder> findByIdAndCustomerId(Long id, Long customerId) {
            throw new UnsupportedOperationException("not used by the review story");
        }

        @Override
        public List<ProgressiveOrder> findAllByCustomerIdOrderByOrderDateDesc(Long customerId) {
            throw new UnsupportedOperationException("not used by the review story");
        }

        @Override
        public Optional<ProgressiveOrder> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<ProgressiveOrder> findAllByStatusOrderByOrderDateAsc(OrderStatus status) {
            return byId.values().stream()
                    .filter(order -> order.getStatus() == status)
                    .sorted(Comparator.comparing(ProgressiveOrder::getOrderDate))
                    .toList();
        }

        @Override
        public List<ProgressiveOrder> findAllByStatusInOrderByOrderDateAsc(Collection<OrderStatus> statuses) {
            throw new UnsupportedOperationException("not used by the review story");
        }
    }

    private static final class FakePrescriptionRepository implements PrescriptionRepository {

        private final Map<Long, Prescription> byId = new HashMap<>();

        void put(Prescription prescription) {
            byId.put(prescription.getId(), prescription);
        }

        @Override
        public Prescription save(Prescription prescription) {
            throw new UnsupportedOperationException("not used by the review story");
        }

        @Override
        public Optional<Prescription> findByIdAndCustomerId(Long id, Long customerId) {
            throw new UnsupportedOperationException("not used by the review story");
        }

        @Override
        public List<Prescription> findAllByCustomerIdOrderByIdDesc(Long customerId) {
            throw new UnsupportedOperationException("not used by the review story");
        }

        @Override
        public Optional<Prescription> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Prescription> findAllByVerificationStatusOrderByIdAsc(VerificationStatus verificationStatus) {
            throw new UnsupportedOperationException("not used by the review story");
        }
    }
}
