package com.myopty.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.order.dto.DiscountRequest;
import com.myopty.order.dto.DiscountResponse;
import com.myopty.order.exception.InvalidDiscountException;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.Discount;
import com.myopty.order.model.DiscountState;
import com.myopty.order.repository.DiscountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * The shop's promotion rules, without a database: that a discount is created with
 * the client's mark, that a bad validity window is refused before it is written,
 * that an ended discount cannot be edited, that ending is a soft and idempotent
 * decision, and how the four states are read.
 *
 * <p>The repository is a hand-written fake because the behaviour under test is
 * the service's own logic — the date rule, the ended-lock and the state
 * derivation — which a fake states directly instead of hiding in a fixture. The
 * real Jackson {@link ObjectMapper} is used, because the JSON round trip through
 * {@code target_item_ids} is part of what has to work.
 */
class DiscountServiceTest {

    private final FakeDiscountRepository discounts = new FakeDiscountRepository();
    private final DiscountService service = new DiscountService(discounts, new ObjectMapper());

    private static DiscountRequest request(List<Long> targetItemIds, LocalDate from, LocalDate to) {
        return new DiscountRequest("Autumn sale", new BigDecimal("15.00"), targetItemIds, from, to);
    }

    private static DiscountRequest openEndedWindow() {
        return request(null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
    }

    @Test
    void creatingStoresThePromotionAndStampsTheClient() {
        DiscountResponse response = service.create(
                7L, request(null, LocalDate.now(), LocalDate.now().plusDays(10)));

        assertThat(response.description()).isEqualTo("Autumn sale");
        assertThat(response.percentage()).isEqualByComparingTo("15.00");
        assertThat(response.active()).isTrue();
        assertThat(response.createdBy()).isEqualTo(7L);
        assertThat(response.state()).isEqualTo(DiscountState.ACTIVE);
        assertThat(discounts.saved.getCreatedBy()).isEqualTo(7L);
    }

    @Test
    void missingTargetsMeanThePromotionIsSitewide() {
        DiscountResponse response = service.create(7L, openEndedWindow());

        assertThat(response.sitewide()).isTrue();
        assertThat(response.targetItemIds()).isNull();
        assertThat(discounts.saved.getTargetItemIds()).isNull();
    }

    @Test
    void targetsRoundTripThroughTheJsonColumn() {
        DiscountResponse response = service.create(7L, request(List.of(3L, 9L), LocalDate.now(), LocalDate.now()));

        assertThat(response.targetItemIds()).containsExactly(3L, 9L);
        assertThat(response.sitewide()).isFalse();
        assertThat(discounts.saved.getTargetItemIds()).isEqualTo("[3,9]");
    }

    @Test
    void anEndBeforeTheStartIsRefusedAndNothingIsWritten() {
        DiscountRequest backwards =
                request(null, LocalDate.now(), LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.create(7L, backwards)).isInstanceOf(InvalidDiscountException.class);
        assertThat(discounts.saved).isNull();
    }

    @Test
    void editingReplacesAnActiveDiscountsDetails() {
        Discount existing = activeDiscount(4L);
        discounts.put(existing);

        DiscountResponse response = service.update(
                4L, request(List.of(2L), LocalDate.now(), LocalDate.now().plusDays(60)));

        assertThat(response.description()).isEqualTo("Autumn sale");
        assertThat(response.targetItemIds()).containsExactly(2L);
        assertThat(discounts.saved.getDescription()).isEqualTo("Autumn sale");
    }

    @Test
    void editingAnEndedDiscountIsRefused() {
        Discount ended = activeDiscount(4L);
        ended.setActive(false);
        ended.setEndedAt(LocalDateTime.now());
        discounts.put(ended);

        assertThatThrownBy(() -> service.update(4L, openEndedWindow())).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void editingADiscountThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.update(404L, openEndedWindow())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void endingFlagsThePromotionWithoutRewritingItsScheduledEnd() {
        Discount existing = activeDiscount(4L);
        LocalDate scheduledEnd = existing.getValidTo();
        discounts.put(existing);

        DiscountResponse response = service.end(4L);

        assertThat(response.active()).isFalse();
        assertThat(response.state()).isEqualTo(DiscountState.ENDED);
        assertThat(response.endedAt()).isNotNull();
        assertThat(response.validTo()).isEqualTo(scheduledEnd);
    }

    @Test
    void endingIsIdempotentAndKeepsTheOriginalEndedAt() {
        Discount existing = activeDiscount(4L);
        existing.setActive(false);
        LocalDateTime firstEnd = LocalDateTime.now().minusDays(3);
        existing.setEndedAt(firstEnd);
        discounts.put(existing);

        DiscountResponse response = service.end(4L);

        assertThat(response.endedAt()).isEqualTo(firstEnd);
    }

    @Test
    void endingADiscountThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.end(404L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listingWithoutAFilterReturnsTheWholeHistoryNewestFirst() {
        discounts.put(activeDiscount(1L));
        Discount ended = activeDiscount(2L);
        ended.setActive(false);
        ended.setEndedAt(LocalDateTime.now());
        discounts.put(ended);

        assertThat(service.list(null)).extracting(DiscountResponse::id).containsExactly(2L, 1L);
    }

    @Test
    void listingActiveNarrowsToWhatIsRunningAndInactiveToTheRest() {
        discounts.put(activeDiscount(1L));
        Discount ended = activeDiscount(2L);
        ended.setActive(false);
        ended.setEndedAt(LocalDateTime.now());
        discounts.put(ended);

        assertThat(service.list(true)).extracting(DiscountResponse::id).containsExactly(1L);
        assertThat(service.list(false)).extracting(DiscountResponse::id).containsExactly(2L);
    }

    @Test
    void stateReadsAsScheduledBeforeTheWindowAndExpiredAfterIt() {
        Discount scheduled = activeDiscount(1L);
        scheduled.setValidFrom(LocalDate.now().plusDays(5));
        scheduled.setValidTo(LocalDate.now().plusDays(10));
        discounts.put(scheduled);

        Discount expired = activeDiscount(2L);
        expired.setValidFrom(LocalDate.now().minusDays(10));
        expired.setValidTo(LocalDate.now().minusDays(5));
        discounts.put(expired);

        Map<Long, DiscountState> byId = new HashMap<>();
        service.list(null).forEach(response -> byId.put(response.id(), response.state()));

        assertThat(byId.get(1L)).isEqualTo(DiscountState.SCHEDULED);
        assertThat(byId.get(2L)).isEqualTo(DiscountState.EXPIRED);
    }

    private static Discount activeDiscount(long id) {
        Discount discount = new Discount();
        discount.setId(id);
        discount.setDescription("Existing promotion");
        discount.setPercentage(new BigDecimal("10.00"));
        discount.setValidFrom(LocalDate.now().minusDays(1));
        discount.setValidTo(LocalDate.now().plusDays(30));
        discount.setActive(true);
        discount.setCreatedBy(7L);
        return discount;
    }

    private static final class FakeDiscountRepository implements DiscountRepository {

        private final Map<Long, Discount> byId = new HashMap<>();
        private Discount saved;

        void put(Discount discount) {
            byId.put(discount.getId(), discount);
        }

        @Override
        public Discount save(Discount discount) {
            if (discount.getId() == null) {
                long next =
                        byId.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
                discount.setId(next);
            }
            this.saved = discount;
            byId.put(discount.getId(), discount);
            return discount;
        }

        @Override
        public Optional<Discount> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Discount> findAllByOrderByIdDesc() {
            return new ArrayList<>(byId.values())
                    .stream()
                            .sorted(Comparator.comparing(Discount::getId, Comparator.reverseOrder()))
                            .toList();
        }
    }
}
