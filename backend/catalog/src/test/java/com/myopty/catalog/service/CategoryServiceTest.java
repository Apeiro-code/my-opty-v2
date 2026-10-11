package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.catalog.exception.InvalidCategoryException;
import com.myopty.catalog.model.Category;
import com.myopty.catalog.model.CategoryItemType;
import com.myopty.catalog.repository.CategoryRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * The filing rules, without a database: that the list comes back oldest first,
 * that no category is a valid choice, and that only a category which accepts the
 * record's type — or is for both — is allowed.
 *
 * <p>The repository is a hand-written fake because what is under test is the
 * choice the service makes, which a fake keeps readable without verification
 * plumbing.
 */
class CategoryServiceTest {

    private final FakeCategoryRepository categories = new FakeCategoryRepository();
    private final CategoryService service = new CategoryService(categories);

    @Test
    void listingReturnsEveryCategoryOldestFirst() {
        categories.store("Men", CategoryItemType.FRAME);
        categories.store("Single Vision", CategoryItemType.LENS);

        assertThat(service.list()).extracting(response -> response.name()).containsExactly("Men", "Single Vision");
    }

    @Test
    void leavingARecordUnfiledIsAllowed() {
        assertThatCode(() -> service.requireCompatible(null, CategoryItemType.FRAME))
                .doesNotThrowAnyException();
    }

    @Test
    void aFrameCategoryAcceptsAFrame() {
        long id = categories.store("Men", CategoryItemType.FRAME);

        assertThatCode(() -> service.requireCompatible(id, CategoryItemType.FRAME))
                .doesNotThrowAnyException();
    }

    @Test
    void aBothCategoryAcceptsALens() {
        long id = categories.store("Clearance", CategoryItemType.BOTH);

        assertThatCode(() -> service.requireCompatible(id, CategoryItemType.LENS))
                .doesNotThrowAnyException();
    }

    @Test
    void aFrameCategoryRefusesALens() {
        long id = categories.store("Men", CategoryItemType.FRAME);

        assertThatThrownBy(() -> service.requireCompatible(id, CategoryItemType.LENS))
                .isInstanceOf(InvalidCategoryException.class);
    }

    @Test
    void aCategoryThatDoesNotExistIsRefused() {
        assertThatThrownBy(() -> service.requireCompatible(99L, CategoryItemType.FRAME))
                .isInstanceOf(InvalidCategoryException.class);
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
