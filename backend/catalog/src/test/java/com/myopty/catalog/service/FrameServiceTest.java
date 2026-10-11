package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.catalog.dto.CreateFrameRequest;
import com.myopty.catalog.dto.FrameResponse;
import com.myopty.catalog.dto.UpdateFrameRequest;
import com.myopty.catalog.exception.InvalidCategoryException;
import com.myopty.catalog.exception.ResourceNotFoundException;
import com.myopty.catalog.model.Category;
import com.myopty.catalog.model.CategoryItemType;
import com.myopty.catalog.model.Frame;
import com.myopty.catalog.repository.CategoryRepository;
import com.myopty.catalog.repository.FrameRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * The add and edit rules, without a database: what a new frame carries, that an
 * omitted {@code active} means on sale, that editing an id nobody stored is a
 * not-found rather than a silent insert, and that a frame may only be filed
 * under a category that accepts frames.
 *
 * <p>The repositories are hand-written fakes because what is under test is the
 * shape of the row the service builds and the checks it makes before writing,
 * which a fake keeps readable without verification plumbing.
 */
class FrameServiceTest {

    private final FakeFrameRepository frames = new FakeFrameRepository();
    private final FakeCategoryRepository categoryRepository = new FakeCategoryRepository();
    private final CategoryService categories = new CategoryService(categoryRepository);
    private final FrameService service = new FrameService(frames, categories);

    @Test
    void creatingAFrameStoresTheDetailsAndDefaultsToActive() {
        FrameResponse response = service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));

        assertThat(response.id()).isPositive();
        assertThat(response.model()).isEqualTo("Astra 2100");
        assertThat(response.color()).isEqualTo("Matte Black");
        assertThat(response.material()).isEqualTo("Acetate");
        assertThat(response.price()).isEqualByComparingTo("4500.00");
        assertThat(response.stockQty()).isEqualTo(12);
        assertThat(response.active()).isTrue();
    }

    @Test
    void anExplicitlyInactiveFrameIsStoredInactive() {
        FrameResponse response = service.create(request("Falcon 330", "Clear", "Acetate", "5400.00", 0, false));

        assertThat(response.active()).isFalse();
    }

    @Test
    void creatingAFrameFilesItUnderTheChosenCategory() {
        long categoryId = categoryRepository.store("Men", CategoryItemType.FRAME);

        FrameResponse response =
                service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null, categoryId));

        assertThat(response.categoryId()).isEqualTo(categoryId);
    }

    @Test
    void aFrameCannotBeFiledUnderALensCategory() {
        long lensCategory = categoryRepository.store("Progressive", CategoryItemType.LENS);

        assertThatThrownBy(() -> service.create(
                        request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null, lensCategory)))
                .isInstanceOf(InvalidCategoryException.class);
    }

    @Test
    void filingUnderACategoryThatDoesNotExistIsRefused() {
        assertThatThrownBy(
                        () -> service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null, 99L)))
                .isInstanceOf(InvalidCategoryException.class);
    }

    @Test
    void editingAFrameReplacesEveryField() {
        FrameResponse created = service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));

        FrameResponse updated = service.update(
                created.id(),
                new UpdateFrameRequest("Astra 2100", "Tortoise", "Acetate", new BigDecimal("4700.00"), 4, true, null));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.color()).isEqualTo("Tortoise");
        assertThat(updated.price()).isEqualByComparingTo("4700.00");
        assertThat(updated.stockQty()).isEqualTo(4);
    }

    @Test
    void editingACompletelyDifferentFrameKeepsTheSameId() {
        FrameResponse first = service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        FrameResponse second = service.create(request("Willow 08", "Plum", "TR-90", "3800.00", 15, null));

        service.update(
                second.id(),
                new UpdateFrameRequest("Willow 08", "Plum", "TR-90", new BigDecimal("3800.00"), 15, false, null));

        assertThat(frames.findById(second.id()).orElseThrow().isActive()).isFalse();
        assertThat(frames.findById(first.id()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void editingAFrameThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.update(
                        99L,
                        new UpdateFrameRequest("Ghost", null, "Acetate", new BigDecimal("1000.00"), 1, true, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void discontinuingAFrameHidesItWithoutRemovingIt() {
        FrameResponse created = service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));

        FrameResponse discontinued = service.discontinue(created.id());

        assertThat(discontinued.active()).isFalse();
        assertThat(frames.findById(created.id())).isPresent();
        assertThat(service.list()).extracting(FrameResponse::id).containsExactly(created.id());
    }

    @Test
    void discontinuingAnAlreadyHiddenFrameStaysHidden() {
        FrameResponse created = service.create(request("Falcon 330", "Clear", "Acetate", "5400.00", 0, false));

        assertThat(service.discontinue(created.id()).active()).isFalse();
    }

    @Test
    void aDiscontinuedFrameCanBeEditedBackOntoTheShopWall() {
        FrameResponse created = service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        service.discontinue(created.id());

        FrameResponse reactivated = service.update(
                created.id(),
                new UpdateFrameRequest(
                        "Astra 2100", "Matte Black", "Acetate", new BigDecimal("4500.00"), 12, true, null));

        assertThat(reactivated.active()).isTrue();
    }

    @Test
    void discontinuingAFrameThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.discontinue(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listingReturnsEveryFrameOldestFirst() {
        service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        service.create(request("Willow 08", "Plum", "TR-90", "3800.00", 15, null));

        assertThat(service.list()).extracting(FrameResponse::model).containsExactly("Astra 2100", "Willow 08");
    }

    private static CreateFrameRequest request(
            String model, String color, String material, String price, int stockQty, Boolean active) {
        return request(model, color, material, price, stockQty, active, null);
    }

    private static CreateFrameRequest request(
            String model, String color, String material, String price, int stockQty, Boolean active, Long categoryId) {
        return new CreateFrameRequest(model, color, material, new BigDecimal(price), stockQty, active, categoryId);
    }

    private static final class FakeFrameRepository implements FrameRepository {

        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, Frame> byId = new HashMap<>();

        @Override
        public Frame save(Frame frame) {
            if (frame.getId() == null) {
                frame.setId(ids.incrementAndGet());
            }
            byId.put(frame.getId(), frame);
            return frame;
        }

        @Override
        public Optional<Frame> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Frame> findAllByOrderByIdAsc() {
            return byId.values().stream()
                    .sorted(Comparator.comparing(Frame::getId))
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
