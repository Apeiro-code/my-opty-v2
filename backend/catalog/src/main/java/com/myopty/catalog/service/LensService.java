package com.myopty.catalog.service;

import com.myopty.catalog.dto.CreateLensRequest;
import com.myopty.catalog.dto.LensResponse;
import com.myopty.catalog.model.Lens;
import com.myopty.catalog.repository.LensRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a lens to the shop's collection.
 *
 * <p>A new lens is saved with no category: this story's form does not choose one,
 * and V4 leaves {@code category_id} nullable so a lens can be filed later. The
 * price and stock rules are enforced by Bean Validation before the service is
 * entered, so the service only has to apply the default and persist.
 */
@Service
public class LensService {

    private final LensRepository lenses;

    public LensService(LensRepository lenses) {
        this.lenses = lenses;
    }

    @Transactional
    public LensResponse create(CreateLensRequest request) {
        Lens lens = new Lens();
        lens.setName(request.name());
        lens.setType(request.type());
        lens.setCoating(request.coating());
        lens.setPrice(request.price());
        lens.setStockQty(request.stockQty());
        lens.setActive(request.active() == null || request.active());
        return LensResponse.from(lenses.save(lens));
    }

    /** Every lens, oldest first, for the shop's collection list. */
    @Transactional(readOnly = true)
    public List<LensResponse> list() {
        return lenses.findAllByOrderByIdAsc().stream().map(LensResponse::from).toList();
    }
}
