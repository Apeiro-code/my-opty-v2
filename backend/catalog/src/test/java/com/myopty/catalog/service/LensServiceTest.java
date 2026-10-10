package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.myopty.catalog.dto.CreateLensRequest;
import com.myopty.catalog.dto.LensResponse;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.LensType;
import com.myopty.catalog.repository.LensRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * The add-lens rule, without a database: what a new lens carries, that an omitted
 * {@code active} means on sale, and that the list comes back oldest first.
 *
 * <p>The repository is a hand-written fake because what is under test is the shape
 * of the row the service builds, which a fake keeps readable without verification
 * plumbing.
 */
class LensServiceTest {

    private final FakeLensRepository lenses = new FakeLensRepository();
    private final LensService service = new LensService(lenses);

    @Test
    void creatingALensStoresTheDetailsAndDefaultsToActive() {
        LensResponse response = service.create(
                request("Thin 1.60 Single Vision", LensType.SINGLE_VISION, "Blue-light filter", "5800.00", 3, null));

        assertThat(response.id()).isPositive();
        assertThat(response.name()).isEqualTo("Thin 1.60 Single Vision");
        assertThat(response.type()).isEqualTo(LensType.SINGLE_VISION);
        assertThat(response.coating()).isEqualTo("Blue-light filter");
        assertThat(response.price()).isEqualByComparingTo("5800.00");
        assertThat(response.stockQty()).isEqualTo(3);
        assertThat(response.active()).isTrue();
    }

    @Test
    void anExplicitlyInactiveLensIsStoredInactive() {
        LensResponse response = service.create(request("Retired Lens", LensType.BIFOCAL, null, "5200.00", 0, false));

        assertThat(response.active()).isFalse();
    }

    @Test
    void listingReturnsEveryLensOldestFirst() {
        service.create(request("Essential Bifocal", LensType.BIFOCAL, "Anti-reflective", "5200.00", 14, null));
        service.create(request("Comfort Progressive", LensType.PROGRESSIVE, "Anti-reflective", "12500.00", 7, null));

        assertThat(service.list())
                .extracting(LensResponse::name)
                .containsExactly("Essential Bifocal", "Comfort Progressive");
    }

    private static CreateLensRequest request(
            String name, LensType type, String coating, String price, int stockQty, Boolean active) {
        return new CreateLensRequest(name, type, coating, new BigDecimal(price), stockQty, active);
    }

    private static final class FakeLensRepository implements LensRepository {

        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, Lens> byId = new HashMap<>();

        @Override
        public Lens save(Lens lens) {
            if (lens.getId() == null) {
                lens.setId(ids.incrementAndGet());
            }
            byId.put(lens.getId(), lens);
            return lens;
        }

        @Override
        public Optional<Lens> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Lens> findAllByOrderByIdAsc() {
            return byId.values().stream()
                    .sorted(Comparator.comparing(Lens::getId))
                    .toList();
        }
    }
}
