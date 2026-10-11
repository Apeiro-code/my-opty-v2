package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.catalog.dto.CreateLensRequest;
import com.myopty.catalog.dto.LensResponse;
import com.myopty.catalog.dto.UpdateLensRequest;
import com.myopty.catalog.exception.InvalidCategoryException;
import com.myopty.catalog.exception.ResourceNotFoundException;
import com.myopty.catalog.model.Category;
import com.myopty.catalog.model.CategoryItemType;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.LensType;
import com.myopty.catalog.repository.CategoryRepository;
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
 * The add and edit rules, without a database: what a new lens carries, that an
 * omitted {@code active} means on sale, that an edit replaces every field, that
 * editing an id nobody stored is a not-found rather than a silent insert, and
 * that a lens may only be filed under a category that accepts lenses.
 *
 * <p>The repositories are hand-written fakes because what is under test is the
 * shape of the row the service builds and the checks it makes before writing,
 * which a fake keeps readable without verification plumbing.
 */
class LensServiceTest {

    private final FakeLensRepository lenses = new FakeLensRepository();
    private final FakeCategoryRepository categoryRepository = new FakeCategoryRepository();
    private final CategoryService categories = new CategoryService(categoryRepository);
    private final LensService service = new LensService(lenses, categories);

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
    void creatingALensFilesItUnderTheChosenCategory() {
        long categoryId = categoryRepository.store("Progressive", CategoryItemType.LENS);

        LensResponse response = service.create(request(
                "Comfort Progressive 1.60", LensType.PROGRESSIVE, "Anti-reflective", "12500.00", 7, null, categoryId));

        assertThat(response.categoryId()).isEqualTo(categoryId);
    }

    @Test
    void aLensCannotBeFiledUnderAFrameCategory() {
        long frameCategory = categoryRepository.store("Men", CategoryItemType.FRAME);

        assertThatThrownBy(() -> service.create(request(
                        "Comfort Progressive 1.60", LensType.PROGRESSIVE, null, "12500.00", 7, null, frameCategory)))
                .isInstanceOf(InvalidCategoryException.class);
    }

    @Test
    void editingALensReplacesEveryField() {
        LensResponse created =
                service.create(request("Essential Bifocal", LensType.BIFOCAL, "Anti-reflective", "5200.00", 14, null));

        LensResponse updated = service.update(
                created.id(),
                new UpdateLensRequest(
                        "Essential Bifocal", LensType.BIFOCAL, "Polarised", new BigDecimal("5400.00"), 9, true, null));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.coating()).isEqualTo("Polarised");
        assertThat(updated.price()).isEqualByComparingTo("5400.00");
        assertThat(updated.stockQty()).isEqualTo(9);
    }

    @Test
    void editingALensCanHideItWithoutRemovingIt() {
        LensResponse created =
                service.create(request("Essential Bifocal", LensType.BIFOCAL, "Anti-reflective", "5200.00", 14, null));

        LensResponse updated = service.update(
                created.id(),
                new UpdateLensRequest(
                        "Essential Bifocal",
                        LensType.BIFOCAL,
                        "Anti-reflective",
                        new BigDecimal("5200.00"),
                        14,
                        false,
                        null));

        assertThat(updated.active()).isFalse();
        assertThat(lenses.findById(created.id())).isPresent();
    }

    @Test
    void editingALensThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.update(
                        99L,
                        new UpdateLensRequest(
                                "Ghost", LensType.SINGLE_VISION, null, new BigDecimal("1000.00"), 1, true, null)))
                .isInstanceOf(ResourceNotFoundException.class);
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
        return request(name, type, coating, price, stockQty, active, null);
    }

    private static CreateLensRequest request(
            String name, LensType type, String coating, String price, int stockQty, Boolean active, Long categoryId) {
        return new CreateLensRequest(name, type, coating, new BigDecimal(price), stockQty, active, categoryId);
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

    private static final class FakeCategoryRepository implements CategoryRepository {

        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, Category> byId = new HashMap<>();

        long store(String name, CategoryItemType itemType) {
            Category category = new Category();
            category.setId(ids.incrementAndGet());
            category.setName(name);
            category.setSlug(name.toLowerCase());
            category.setItemType(itemType);
            byId.put(category.getId(), category);
            return category.getId();
        }

        @Override
        public Optional<Category> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Category> findAllByOrderByIdAsc() {
            return byId.values().stream()
                    .sorted(Comparator.comparing(Category::getId))
                    .toList();
        }
    }
}
