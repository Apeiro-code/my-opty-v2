package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.catalog.dto.CreateFrameRequest;
import com.myopty.catalog.dto.FrameResponse;
import com.myopty.catalog.dto.StorefrontFrameResponse;
import com.myopty.catalog.dto.UpdateFrameRequest;
import com.myopty.catalog.exception.InvalidCategoryException;
import com.myopty.catalog.exception.ResourceNotFoundException;
import com.myopty.catalog.model.Category;
import com.myopty.catalog.model.CategoryItemType;
import com.myopty.catalog.model.Frame;
import com.myopty.catalog.repository.CategoryRepository;
import com.myopty.catalog.repository.FrameRepository;
import java.math.BigDecimal;
import java.util.Collections;
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

    @Test
    void theStorefrontListsOnlyActiveFramesOldestFirst() {
        service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        service.create(request("Falcon 330", "Clear", "Acetate", "5400.00", 0, false));
        service.create(request("Willow 08", "Plum", "TR-90", "3800.00", 15, null));

        assertThat(service.listActiveForStorefront())
                .extracting(StorefrontFrameResponse::model)
                .containsExactly("Astra 2100", "Willow 08");
    }

    @Test
    void theStorefrontCanBeFilteredByCategorySlug() {
        long men = categoryRepository.store("Men", CategoryItemType.FRAME);
        long women = categoryRepository.store("Women", CategoryItemType.FRAME);
        FrameResponse menFrame =
                service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null, men));
        service.create(request("Meridian", "Champagne", "Titanium", "9200.00", 6, null, women));

        assertThat(service.listActiveForStorefrontFiltered("men", null, null, null, null, null))
                .extracting(StorefrontFrameResponse::id)
                .containsExactly(menFrame.id());
    }

    @Test
    void theStorefrontFiltersByPriceRangeInclusively() {
        service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        FrameResponse mid = service.create(request("Kestrel 04", "Gunmetal", "Steel", "6800.00", 8, null));
        service.create(request("Meridian", "Champagne", "Titanium", "9200.00", 6, null));

        assertThat(service.listActiveForStorefrontFiltered(
                        null, new BigDecimal("5000.00"), new BigDecimal("8000.00"), null, null, null))
                .extracting(StorefrontFrameResponse::id)
                .containsExactly(mid.id());
    }

    @Test
    void theStorefrontFiltersByColorAndMaterialCaseInsensitively() {
        FrameResponse black = service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        service.create(request("Willow 08", "Plum", "TR-90", "3800.00", 15, null));
        FrameResponse titanium = service.create(request("Meridian", "Rose Gold", "Titanium", "9200.00", 6, null));

        assertThat(service.listActiveForStorefrontFiltered(null, null, null, "black", null, null))
                .extracting(StorefrontFrameResponse::id)
                .containsExactly(black.id());
        assertThat(service.listActiveForStorefrontFiltered(null, null, null, null, "titanium", null))
                .extracting(StorefrontFrameResponse::id)
                .containsExactly(titanium.id());
    }

    @Test
    void theStorefrontCombinesFiltersWithAnd() {
        long men = categoryRepository.store("Men", CategoryItemType.FRAME);
        service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null, men));
        FrameResponse match = service.create(request("Kestrel 04", "Gunmetal", "Steel", "6800.00", 8, null, men));
        service.create(request("Meridian", "Champagne", "Titanium", "9200.00", 6, null, men));

        assertThat(service.listActiveForStorefrontFiltered(
                        "men", new BigDecimal("5000.00"), new BigDecimal("8000.00"), "gunmetal", "steel", null))
                .extracting(StorefrontFrameResponse::id)
                .containsExactly(match.id());
    }

    @Test
    void theStorefrontReturnsEmptyWhenNoFramesMatchFilters() {
        service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        assertThat(service.listActiveForStorefrontFiltered("women", null, null, null, null, null))
                .isEmpty();
    }

    @Test
    void theStorefrontReportsThumbnailAndFriendlyStock() {
        FrameResponse inStock = service.create(request("Astra 2100", "Matte Black", "Acetate", "4500.00", 12, null));
        FrameResponse soldOut = service.create(request("Willow 08", "Plum", "TR-90", "3800.00", 0, null));
        frames.findById(inStock.id()).orElseThrow().setImageUrl("/images/frames/astra-2100.jpg");

        List<StorefrontFrameResponse> listed = service.listActiveForStorefront();

        assertThat(listed)
                .extracting(StorefrontFrameResponse::imageUrl)
                .containsExactly("/images/frames/astra-2100.jpg", null);
        assertThat(listed).extracting(StorefrontFrameResponse::inStock).containsExactly(true, false);
        assertThat(listed).extracting(StorefrontFrameResponse::id).containsExactly(inStock.id(), soldOut.id());
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
        private final Map<Long, com.myopty.catalog.model.Category> categories = new HashMap<>();

        @Override
        public Frame save(Frame frame) {
            if (frame.getId() == null) {
                frame.setId(ids.incrementAndGet());
            }
            byId.put(frame.getId(), frame);
            return frame;
        }

        void addCategory(Category category) {
            if (category != null && category.getId() != null) {
                categories.put(category.getId(), category);
            }
        }

        @Override
        public Optional<Frame> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Frame> findByIdAndActiveTrue(Long id) {
            return findById(id).filter(Frame::isActive);
        }

        @Override
        public List<Frame> findNewArrivals(int limit) {
            return Collections.emptyList();
        }

        @Override
        public List<Frame> findAllByOrderByIdAsc() {
            return byId.values().stream()
                    .sorted(Comparator.comparing(Frame::getId))
                    .toList();
        }

        @Override
        public List<Frame> findAllByActiveTrueOrderByIdAsc() {
            return byId.values().stream()
                    .filter(Frame::isActive)
                    .sorted(Comparator.comparing(Frame::getId))
                    .toList();
        }

        @Override
        public List<Frame> findActiveForStorefront(
                String categorySlug,
                java.math.BigDecimal minPrice,
                java.math.BigDecimal maxPrice,
                String color,
                String material,
                String q) {
            return byId.values().stream()
                    .filter(Frame::isActive)
                    .filter(frame -> {
                        if (categorySlug != null) {
                            if (categories.get(frame.getCategoryId()) == null) {
                                return false;
                            }
                            String slug = categories.get(frame.getCategoryId()).getSlug();
                            if (slug == null || !slug.equalsIgnoreCase(categorySlug)) {
                                return false;
                            }
                        }
                        if (minPrice != null
                                && frame.getPrice() != null
                                && frame.getPrice().compareTo(minPrice) < 0) {
                            return false;
                        }
                        if (maxPrice != null
                                && frame.getPrice() != null
                                && frame.getPrice().compareTo(maxPrice) > 0) {
                            return false;
                        }
                        if (color != null && !color.isBlank()) {
                            String c = frame.getColor();
                            if (c == null || !c.toLowerCase().contains(color.toLowerCase())) {
                                return false;
                            }
                        }
                        if (material != null && !material.isBlank()) {
                            String m = frame.getMaterial();
                            if (m == null || !m.toLowerCase().contains(material.toLowerCase())) {
                                return false;
                            }
                        }
                        if (q != null && !q.trim().isEmpty()) {
                            String model = frame.getModel();
                            if (model == null
                                    || !model.toLowerCase().contains(q.trim().toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(Frame::getId))
                    .toList();
        }

        @Override
        public int decrementStock(long id, int quantity) {
            Frame frame = byId.get(id);
            if (frame == null || frame.getStockQty() < quantity) {
                return 0;
            }
            frame.setStockQty(frame.getStockQty() - quantity);
            return 1;
        }

        @Override
        public int incrementStock(long id, int quantity) {
            Frame frame = byId.get(id);
            if (frame == null) {
                return 0;
            }
            frame.setStockQty(frame.getStockQty() + quantity);
            return 1;
        }
    }

    private final class FakeCategoryRepository implements CategoryRepository {

        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, Category> byId = new HashMap<>();

        long store(String name, CategoryItemType itemType) {
            Category category = new Category();
            category.setId(ids.incrementAndGet());
            category.setName(name);
            category.setSlug(name.toLowerCase());
            category.setItemType(itemType);
            byId.put(category.getId(), category);
            // also sync to frames fake for filtering tests
            try {
                java.lang.reflect.Field f = frames.getClass().getDeclaredField("categories");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<Long, Category> map = (java.util.Map<Long, Category>) f.get(frames);
                map.put(category.getId(), category);
            } catch (Exception ignored) {
            }
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
