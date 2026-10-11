package com.myopty.catalog.repository;

import com.myopty.catalog.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Reads {@code category} for the shop owner's filing picker.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this story
 * lists the categories a frame or lens may be filed under and checks one before
 * a save. Creating, renaming and nesting categories are later stories and will
 * widen it deliberately with the writes they need.
 */
public interface CategoryRepository extends Repository<Category, Long> {

    Optional<Category> findById(Long id);

    List<Category> findAllByOrderByIdAsc();
}
