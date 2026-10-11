package com.myopty.order.service;

import com.myopty.contracts.order.AppliedDiscount;
import com.myopty.contracts.order.ItemKind;
import com.myopty.order.dto.DiscountRequest;
import com.myopty.order.dto.DiscountResponse;
import com.myopty.order.exception.InvalidDiscountException;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.Discount;
import com.myopty.order.model.DiscountState;
import com.myopty.order.repository.DiscountRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * The shop's promotions: creating one, keeping it accurate, reading the history,
 * and calling one off before its end date.
 *
 * <p>The validity window's one cross-field rule — an end that is not before the
 * start — is checked here because Bean Validation annotates one field at a time;
 * the single-field bounds on percentage and the required dates are already
 * enforced before the service is entered, which is also what keeps a bad value
 * from reaching V103's {@code chk_discount_percentage} and surfacing as a
 * constraint failure.
 *
 * <p>Ending is a soft decision, not a delete: the row keeps its data and its
 * original {@code valid_to} so the promotion history still shows what it was
 * scheduled to do. The state a caller sees is derived at read time from the flag
 * and the dates, so nothing has to sweep rows when a window closes.
 *
 * <p>{@code target_item_ids} is stored as JSON text; the application's Jackson
 * {@link ObjectMapper} encodes and decodes it, which is why the entity holds a
 * {@code String} and this service is where a {@code List<Long>} becomes one.
 */
@Service
public class DiscountService {

    private static final TypeReference<List<Long>> LONG_LIST = new TypeReference<>() {};

    private final DiscountRepository discounts;
    private final ObjectMapper objectMapper;

    public DiscountService(DiscountRepository discounts, ObjectMapper objectMapper) {
        this.discounts = discounts;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DiscountResponse create(long clientId, DiscountRequest request) {
        requireValidWindow(request.validFrom(), request.validTo());
        Discount discount = new Discount();
        apply(discount, request);
        discount.setActive(true);
        discount.setCreatedBy(clientId);
        Discount saved = discounts.save(discount);
        // created_at is the column's default, not a value this code set, so the
        // saved row is read back to report it; the insert itself stays in the same
        // transaction, so the read sees what was just written.
        return toResponse(discounts.findById(saved.getId()).orElse(saved));
    }

    /**
     * Replaces an active or scheduled promotion's details. A promotion the shop
     * has already ended is frozen: reviving it would rewrite history, so the
     * answer is to create a new one instead.
     */
    @Transactional
    public DiscountResponse update(long id, DiscountRequest request) {
        requireValidWindow(request.validFrom(), request.validTo());
        Discount discount =
                discounts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Discount not found."));
        requireNotEnded(discount);
        apply(discount, request);
        return toResponse(discounts.save(discount));
    }

    /**
     * Ends a promotion early. Idempotent: ending one that is already ended leaves
     * its original {@code ended_at} untouched rather than moving the date, so
     * calling this twice is harmless and does not rewrite when it stopped.
     */
    @Transactional
    public DiscountResponse end(long id) {
        Discount discount =
                discounts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Discount not found."));
        if (discount.isActive() && discount.getEndedAt() == null) {
            discount.setActive(false);
            discount.setEndedAt(LocalDateTime.now());
            discount = discounts.save(discount);
        }
        return toResponse(discount);
    }

    /**
     * Every promotion, newest first. A null {@code active} returns the whole
     * history; {@code true} narrows to what is running now and {@code false} to
     * everything that is not. {@code state} narrows to one derived state, for a
     * caller that would rather the server did the filtering.
     */
    @Transactional(readOnly = true)
    public List<DiscountResponse> list(Boolean active, DiscountState state) {
        LocalDate today = LocalDate.now();
        return discounts.findAllByOrderByIdDesc().stream()
                .map(discount -> toResponse(discount, today))
                .filter(response -> active == null || active == (response.state() == DiscountState.ACTIVE))
                .filter(response -> state == null || state == response.state())
                .toList();
    }

    /**
     * The single promotion a customer's item would get, for the storefront to
     * price against: item-specific beats sitewide, then the larger percentage
     * wins. An empty result means the item is full price.
     *
     * <p>Only {@code ACTIVE} promotions count — a scheduled one has not started
     * and an expired or ended one has stopped. {@code kind} is not consulted: the
     * stored targets are a flat list with no type, so an id matches a frame or a
     * lens alike (the trade V103 documents). It is in the signature because a
     * typed target model will need it.
     */
    @Transactional(readOnly = true)
    public Optional<AppliedDiscount> bestDiscountFor(ItemKind kind, long itemId) {
        LocalDate today = LocalDate.now();
        List<Discount> running = discounts.findAllByActiveTrue().stream()
                .filter(discount -> stateOf(discount, today) == DiscountState.ACTIVE)
                .toList();

        Optional<Discount> itemSpecific = running.stream()
                .filter(discount -> targets(discount).contains(itemId))
                .max(Comparator.comparing(Discount::getPercentage));
        Optional<Discount> sitewide = running.stream()
                .filter(discount -> targets(discount).isEmpty())
                .max(Comparator.comparing(Discount::getPercentage));

        return itemSpecific.or(() -> sitewide).map(DiscountService::toApplied);
    }

    private void apply(Discount discount, DiscountRequest request) {
        discount.setDescription(request.description().trim());
        discount.setPercentage(request.percentage());
        discount.setTargetItemIds(encodeTargets(request.targetItemIds()));
        discount.setValidFrom(request.validFrom());
        discount.setValidTo(request.validTo());
    }

    private DiscountResponse toResponse(Discount discount) {
        return toResponse(discount, LocalDate.now());
    }

    private DiscountResponse toResponse(Discount discount, LocalDate today) {
        return DiscountResponse.from(discount, decodeTargets(discount.getTargetItemIds()), stateOf(discount, today));
    }

    private String encodeTargets(List<Long> targetItemIds) {
        if (targetItemIds == null || targetItemIds.isEmpty()) {
            return null;
        }
        return objectMapper.writeValueAsString(targetItemIds);
    }

    private List<Long> decodeTargets(String targetItemIds) {
        if (targetItemIds == null || targetItemIds.isBlank()) {
            return null;
        }
        return objectMapper.readValue(targetItemIds, LONG_LIST);
    }

    private List<Long> targets(Discount discount) {
        List<Long> decoded = decodeTargets(discount.getTargetItemIds());
        return decoded == null ? List.of() : decoded;
    }

    private static AppliedDiscount toApplied(Discount discount) {
        return new AppliedDiscount(discount.getPercentage(), discount.getDescription());
    }

    private static DiscountState stateOf(Discount discount, LocalDate today) {
        if (!discount.isActive() || discount.getEndedAt() != null) {
            return DiscountState.ENDED;
        }
        if (today.isBefore(discount.getValidFrom())) {
            return DiscountState.SCHEDULED;
        }
        if (today.isAfter(discount.getValidTo())) {
            return DiscountState.EXPIRED;
        }
        return DiscountState.ACTIVE;
    }

    private static void requireValidWindow(LocalDate validFrom, LocalDate validTo) {
        if (validFrom == null || validTo == null) {
            throw new InvalidDiscountException("A start and end date are required.");
        }
        if (validTo.isBefore(validFrom)) {
            throw new InvalidDiscountException("The end date cannot be before the start date.");
        }
    }

    private static void requireNotEnded(Discount discount) {
        if (!discount.isActive() || discount.getEndedAt() != null) {
            throw new InvalidStateException("This discount has already ended.");
        }
    }
}
