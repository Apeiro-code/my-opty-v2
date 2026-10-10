package com.myopty.catalog.repository;

import com.myopty.catalog.model.Lens;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Writes and reads {@code lens}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this story
 * adds a lens and lists them for the shop. The edit, discontinue and
 * customer-facing browsing stories will widen it deliberately with the queries
 * they need, rather than inheriting methods this module does not use yet.
 */
public interface LensRepository extends Repository<Lens, Long> {

    Lens save(Lens lens);

    Optional<Lens> findById(Long id);

    List<Lens> findAllByOrderByIdAsc();
}
