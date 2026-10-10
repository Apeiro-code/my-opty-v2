package com.myopty.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.order.dto.CreateOrderRequest;
import com.myopty.order.dto.OrderResponse;
import com.myopty.order.exception.PrescriptionRejectedException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.OrderType;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
import com.myopty.order.repository.ProgressiveOrderRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * The order creation rules, without a database: which prescription may be linked,
 * what the created order carries, and that an order cannot be read by anyone but
 * its owner.
 *
 * <p>The repositories are hand-written fakes because what is under test is the
 * shape of the row the service builds and the checks it makes before building it,
 * which a fake keeps readable without verification plumbing.
 */
class OrderServiceTest {

    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final FakePrescriptionRepository prescriptions = new FakePrescriptionRepository();
    private final OrderService service = new OrderService(orders, prescriptions);

    private static Prescription prescription(long id, long customerId, VerificationStatus status) {
        Prescription prescription = new Prescription();
        prescription.setId(id);
        prescription.setCustomerId(customerId);
        prescription.setVerificationStatus(status);
        return prescription;
    }

    @Test
    void creatingAnOrderLinksThePrescriptionFrameAndLens() {
        prescriptions.put(prescription(4L, 7L, VerificationStatus.VERIFIED));

        OrderResponse response = service.create(7L, new CreateOrderRequest(4L, OrderType.PROGRESSIVE, 12L, 3L, null));

        assertThat(response.prescriptionId()).isEqualTo(4L);
        assertThat(response.frameId()).isEqualTo(3L);
        assertThat(response.lensId()).isEqualTo(12L);
        assertThat(response.orderType()).isEqualTo(OrderType.PROGRESSIVE);
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.orderNumber()).startsWith("ORD-");
        assertThat(response.orderDate()).isNotNull();
        assertThat(response.totalAmount()).isNull();
    }

    @Test
    void aLensesOnlyOrderHasNoFrame() {
        prescriptions.put(prescription(4L, 7L, VerificationStatus.PENDING_REVIEW));

        OrderResponse response =
                service.create(7L, new CreateOrderRequest(4L, OrderType.SINGLE_VISION, 12L, null, null));

        assertThat(response.frameId()).isNull();
    }

    @Test
    void theRequestedQuantityIsKept() {
        prescriptions.put(prescription(4L, 7L, VerificationStatus.VERIFIED));

        OrderResponse response = service.create(7L, new CreateOrderRequest(4L, OrderType.BIFOCAL, 12L, 3L, 2));

        assertThat(response.quantity()).isEqualTo(2);
    }

    @Test
    void anotherCustomersPrescriptionIsNotFound() {
        prescriptions.put(prescription(4L, 99L, VerificationStatus.VERIFIED));

        assertThatThrownBy(() -> service.create(7L, new CreateOrderRequest(4L, OrderType.PROGRESSIVE, 12L, 3L, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aRejectedPrescriptionCannotBeOrdered() {
        prescriptions.put(prescription(4L, 7L, VerificationStatus.REJECTED));

        assertThatThrownBy(() -> service.create(7L, new CreateOrderRequest(4L, OrderType.PROGRESSIVE, 12L, 3L, null)))
                .isInstanceOf(PrescriptionRejectedException.class);
        assertThat(orders.saved).isNull();
    }

    @Test
    void readingAnOrderBackRequiresTheOwner() {
        Prescription source = prescription(4L, 7L, VerificationStatus.VERIFIED);
        prescriptions.put(source);
        OrderResponse created = service.create(7L, new CreateOrderRequest(4L, OrderType.PROGRESSIVE, 12L, 3L, null));

        assertThat(service.readOwn(7L, created.id()).orderNumber()).isEqualTo(created.orderNumber());
        assertThatThrownBy(() -> service.readOwn(8L, created.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    private static final class FakeOrderRepository implements ProgressiveOrderRepository {

        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, ProgressiveOrder> byId = new HashMap<>();
        private ProgressiveOrder saved;

        @Override
        public ProgressiveOrder save(ProgressiveOrder order) {
            order.setId(ids.incrementAndGet());
            byId.put(order.getId(), order);
            this.saved = order;
            return order;
        }

        @Override
        public Optional<ProgressiveOrder> findByIdAndCustomerId(Long id, Long customerId) {
            return Optional.ofNullable(byId.get(id))
                    .filter(order -> order.getCustomerId().equals(customerId));
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
    }

    private static final class FakePrescriptionRepository implements PrescriptionRepository {

        private final Map<Long, Prescription> byId = new HashMap<>();

        void put(Prescription prescription) {
            byId.put(prescription.getId(), prescription);
        }

        @Override
        public Prescription save(Prescription prescription) {
            throw new UnsupportedOperationException("not used by the order story");
        }

        @Override
        public Optional<Prescription> findByIdAndCustomerId(Long id, Long customerId) {
            return Optional.ofNullable(byId.get(id))
                    .filter(prescription -> prescription.getCustomerId().equals(customerId));
        }

        @Override
        public List<Prescription> findAllByCustomerIdOrderByIdDesc(Long customerId) {
            return byId.values().stream()
                    .filter(prescription -> prescription.getCustomerId().equals(customerId))
                    .sorted(Comparator.comparing(Prescription::getId).reversed())
                    .toList();
        }

        @Override
        public Optional<Prescription> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Prescription> findAllByVerificationStatusOrderByIdAsc(VerificationStatus verificationStatus) {
            return byId.values().stream()
                    .filter(prescription -> prescription.getVerificationStatus() == verificationStatus)
                    .sorted(Comparator.comparing(Prescription::getId))
                    .toList();
        }
    }
}
