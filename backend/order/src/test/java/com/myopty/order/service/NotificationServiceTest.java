package com.myopty.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.order.config.NotificationProperties;
import com.myopty.order.dto.NotificationResponse;
import com.myopty.order.exception.NotificationDeliveryException;
import com.myopty.order.model.NotificationChannel;
import com.myopty.order.model.NotificationStatus;
import com.myopty.order.model.OrderNotification;
import com.myopty.order.model.OrderStatus;
import com.myopty.order.model.OrderType;
import com.myopty.order.model.ProgressiveOrder;
import com.myopty.order.repository.OrderNotificationRepository;
import com.myopty.order.repository.ProgressiveOrderRepository;
import com.myopty.shared.user.AccountStatus;
import com.myopty.shared.user.AppUser;
import com.myopty.shared.user.AppUserRepository;
import com.myopty.shared.user.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * What is stored and what is sent when an order moves: the verbatim message, the
 * {@code SENT}/{@code FAILED} outcome, and whether a failed send becomes an error.
 *
 * <p>The sender is a recorder that can be told to fail, so the two branches are
 * exercised without a mail server, and the notification repository is a fake so
 * the assertion reads the row that would have been written.
 */
class NotificationServiceTest {

    private final FakeNotificationRepository notifications = new FakeNotificationRepository();
    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final FakeUserRepository users = new FakeUserRepository();
    private final RecordingSender sender = new RecordingSender();
    private final NotificationProperties properties = new NotificationProperties();

    private NotificationService service() {
        return new NotificationService(notifications, orders, users, sender, properties);
    }

    private static ProgressiveOrder order(long id, OrderStatus status) {
        ProgressiveOrder order = new ProgressiveOrder();
        order.setId(id);
        order.setOrderNumber("ORD-TEST-" + id);
        order.setCustomerId(7L);
        order.setOrderType(OrderType.PROGRESSIVE);
        order.setStatus(status);
        order.setOrderDate(LocalDateTime.of(2026, 10, 10, 9, 0));
        return order;
    }

    private void aCustomerExists() {
        users.put(new AppUser(
                7L, "ada@example.com", null, null, "Ada", Role.CUSTOMER, AccountStatus.ACTIVE, true, false));
    }

    @Test
    void anApprovalMessageCarriesTheReceiveDateAndIsMarkedSent() {
        aCustomerExists();
        ProgressiveOrder approved = order(4L, OrderStatus.APPROVED);
        approved.setReceiveDate(LocalDate.of(2026, 10, 20));
        orders.put(approved);

        service().recordAndSend(approved, OrderStatus.APPROVED);

        OrderNotification stored = notifications.only();
        assertThat(stored.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(stored.getSentAt()).isNotNull();
        assertThat(stored.getMessage()).contains("ORD-TEST-4").contains("2026-10-20");
        assertThat(sender.recipient).isEqualTo("ada@example.com");
        assertThat(sender.message).isEqualTo(stored.getMessage());
    }

    @Test
    void aRejectionMessageCarriesTheReason() {
        aCustomerExists();
        ProgressiveOrder rejected = order(4L, OrderStatus.REJECTED);
        rejected.setRejectionReason("Lens out of stock.");
        orders.put(rejected);

        service().recordAndSend(rejected, OrderStatus.REJECTED);

        assertThat(notifications.only().getMessage()).contains("Lens out of stock.");
    }

    @Test
    void aSendThatFailsIsRecordedButNotRaisedByDefault() {
        aCustomerExists();
        sender.fail = true;
        ProgressiveOrder ready = order(4L, OrderStatus.READY);
        orders.put(ready);

        service().recordAndSend(ready, OrderStatus.READY);

        OrderNotification stored = notifications.only();
        assertThat(stored.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(stored.getSentAt()).isNull();
    }

    @Test
    void aSendThatFailsIsRaisedWhenFailOnErrorIsOn() {
        aCustomerExists();
        sender.fail = true;
        properties.setFailOnError(true);
        ProgressiveOrder ready = order(4L, OrderStatus.READY);
        orders.put(ready);

        assertThatThrownBy(() -> service().recordAndSend(ready, OrderStatus.READY))
                .isInstanceOf(NotificationDeliveryException.class);
        assertThat(notifications.only().getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void aCustomerWithNoEmailIsRecordedAsFailedAndNeverSent() {
        ProgressiveOrder dispatched = order(4L, OrderStatus.DISPATCHED);
        orders.put(dispatched);

        service().recordAndSend(dispatched, OrderStatus.DISPATCHED);

        assertThat(notifications.only().getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(sender.recipient).isNull();
    }

    @Test
    void theCustomersNotificationsComeBackNewestFirstWithTheirOrderNumber() {
        aCustomerExists();
        orders.put(order(4L, OrderStatus.PROCESSING));
        ProgressiveOrder first = order(4L, OrderStatus.APPROVED);
        service().recordAndSend(first, OrderStatus.APPROVED);
        service().recordAndSend(first, OrderStatus.PROCESSING);

        List<NotificationResponse> list = service().listForCustomer(7L, null);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).orderNumber()).isEqualTo("ORD-TEST-4");
        assertThat(list.get(0).id()).isGreaterThan(list.get(1).id());
        assertThat(list.get(0).channel()).isEqualTo(NotificationChannel.EMAIL);
    }

    @Test
    void pendingIsNotAStatusTheCustomerIsToldAbout() {
        aCustomerExists();
        ProgressiveOrder pending = order(4L, OrderStatus.PENDING);

        assertThatThrownBy(() -> service().recordAndSend(pending, OrderStatus.PENDING))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class RecordingSender implements NotificationSender {

        private String recipient;
        private String message;
        private boolean fail;

        @Override
        public void send(OrderNotification notification, String recipientEmail) {
            if (fail) {
                throw new IllegalStateException("smtp down");
            }
            this.recipient = recipientEmail;
            this.message = notification.getMessage();
        }
    }

    private static final class FakeNotificationRepository implements OrderNotificationRepository {

        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, OrderNotification> byId = new HashMap<>();

        OrderNotification only() {
            return byId.values().iterator().next();
        }

        @Override
        public OrderNotification save(OrderNotification notification) {
            if (notification.getId() == null) {
                notification.setId(ids.incrementAndGet());
            }
            byId.put(notification.getId(), notification);
            return notification;
        }

        @Override
        public List<OrderNotification> findAllByCustomerIdOrderByIdDesc(Long customerId) {
            return matching(row -> row.getCustomerId().equals(customerId));
        }

        @Override
        public List<OrderNotification> findAllByCustomerIdAndOrderIdOrderByIdDesc(Long customerId, Long orderId) {
            return matching(row ->
                    row.getCustomerId().equals(customerId) && row.getOrderId().equals(orderId));
        }

        @Override
        public List<OrderNotification> findAllByOrderIdOrderByIdDesc(Long orderId) {
            return matching(row -> row.getOrderId().equals(orderId));
        }

        private List<OrderNotification> matching(java.util.function.Predicate<OrderNotification> predicate) {
            return byId.values().stream()
                    .filter(predicate)
                    .sorted(Comparator.comparing(OrderNotification::getId).reversed())
                    .toList();
        }
    }

    private static final class FakeUserRepository implements AppUserRepository {

        private final Map<Long, AppUser> byId = new HashMap<>();

        void put(AppUser user) {
            byId.put(user.getId(), user);
        }

        @Override
        public Optional<AppUser> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<AppUser> findByEmail(String email) {
            throw new UnsupportedOperationException("not used by the notification story");
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
            throw new UnsupportedOperationException("not used by the notification story");
        }

        @Override
        public List<ProgressiveOrder> findAllByCustomerIdOrderByOrderDateDesc(Long customerId) {
            throw new UnsupportedOperationException("not used by the notification story");
        }

        @Override
        public Optional<ProgressiveOrder> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<ProgressiveOrder> findAllByStatusOrderByOrderDateAsc(OrderStatus status) {
            throw new UnsupportedOperationException("not used by the notification story");
        }

        @Override
        public List<ProgressiveOrder> findAllByStatusInOrderByOrderDateAsc(Collection<OrderStatus> statuses) {
            throw new UnsupportedOperationException("not used by the notification story");
        }
    }
}
