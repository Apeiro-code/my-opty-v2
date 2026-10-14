package com.myopty.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.myopty.catalog.dto.StorefrontLensResponse;
import com.myopty.catalog.model.Category;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.model.LensType;
import com.myopty.catalog.repository.LensRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LensServiceTest {

    @Test
    void listsActiveLensesForStorefrontOnly() {
        Lens active = new Lens();
        active.setId(1L);
        active.setName("Classic");
        active.setType(LensType.SINGLE_VISION);
        active.setCoating("Anti-Reflective");
        active.setPrice(new BigDecimal("2500.00"));
        active.setStockQty(5);
        active.setActive(true);
        active.setCategoryId(1L);
        active.setImageUrl("/img/l1.png");

        Lens inactive = new Lens();
        inactive.setId(2L);
        inactive.setName("Old");
        inactive.setType(LensType.BIFOCAL);
        inactive.setCoating(null);
        inactive.setPrice(new BigDecimal("2000.00"));
        inactive.setStockQty(3);
        inactive.setActive(false);

        FakeLensRepository fake = new FakeLensRepository(List.of(active, inactive));
        CategoryService categories = new CategoryService(new InMemoryCategoryRepository());
        LensService service = new LensService(fake, categories);

        List<StorefrontLensResponse> result = service.listActiveForStorefront();
        assertThat(result).hasSize(1);
        StorefrontLensResponse r = result.get(0);
        assertThat(r.id()).isEqualTo(1L);
        assertThat(r.name()).isEqualTo("Classic");
        assertThat(r.inStock()).isTrue();
        assertThat(r.imageUrl()).isEqualTo("/img/l1.png");
    }

    static class FakeLensRepository implements LensRepository {
        private final List<Lens> store = new ArrayList<>();

        FakeLensRepository(List<Lens> initial) {
            store.addAll(initial);
        }

        @Override
        public Lens save(Lens lens) {
            store.removeIf(l -> lens.getId() != null && l.getId().equals(lens.getId()));
            if (lens.getId() == null) {
                lens.setId((long) (store.size() + 1));
            }
            store.add(lens);
            return lens;
        }

        @Override
        public Optional<Lens> findById(Long id) {
            return store.stream().filter(l -> id.equals(l.getId())).findFirst();
        }

        @Override
        public List<Lens> findAllByOrderByIdAsc() {
            return store.stream()
                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                    .toList();
        }

        @Override
        public List<Lens> findAllByActiveTrueOrderByIdAsc() {
            return store.stream()
                    .filter(Lens::isActive)
                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                    .toList();
        }

        @Override
        public int decrementStock(long id, int quantity) {
            Optional<Lens> lensOpt = findById(id);
            if (lensOpt.isEmpty()) {
                return 0;
            }
            Lens lens = lensOpt.get();
            if (lens.getStockQty() < quantity) {
                return 0;
            }
            lens.setStockQty(lens.getStockQty() - quantity);
            return 1;
        }

        @Override
        public int incrementStock(long id, int quantity) {
            Optional<Lens> lensOpt = findById(id);
            if (lensOpt.isEmpty()) {
                return 0;
            }
            Lens lens = lensOpt.get();
            lens.setStockQty(lens.getStockQty() + quantity);
            return 1;
        }
    }

    static class InMemoryCategoryRepository implements com.myopty.catalog.repository.CategoryRepository {
        private final java.util.Map<Long, Category> byId = new java.util.HashMap<>();

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
