package com.myopty.catalog.repository;

import com.myopty.catalog.model.Lens;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Writes and reads {@code lens}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this story
 * adds a lens and lists them for the shop. The edit, discontinue and
 * customer-facing browsing stories will widen it deliberately with the queries
 * they need, rather than inheriting methods this module does not use yet.
 *
 * <p>Stock moves are single conditional {@code UPDATE}s rather than
 * read-modify-write through the entity; see {@link FrameRepository} for why.
 */
public interface LensRepository extends Repository<Lens, Long> {

    Lens save(Lens lens);

    Optional<Lens> findById(Long id);

    List<Lens> findAllByOrderByIdAsc();

    /** Active lenses for storefront, oldest first. */
    List<Lens> findAllByActiveTrueOrderByIdAsc();

    List<Lens> findAllByActiveTrueAndTypeOrderByIdAsc(com.myopty.catalog.model.LensType type);

    @Query("""
            SELECT * FROM lens WHERE is_active = TRUE ORDER BY created_at DESC, id DESC LIMIT :limit
            """)
    List<Lens> findNewArrivals(@Param("limit") int limit);

    java.util.Optional<Lens> findByIdAndActiveTrue(Long id);

    /**
     * Takes {@code quantity} off a lens in one statement. Returns the number of
     * rows changed, which is zero — and nothing is modified — when the lens
     * does not have that many left.
     */
    @Modifying
    @Query("UPDATE lens SET stock_qty = stock_qty - :quantity WHERE id = :id AND stock_qty >= :quantity")
    int decrementStock(@Param("id") long id, @Param("quantity") int quantity);

    /** Puts {@code quantity} back on a lens. Returns the number of rows changed. */
    @Modifying
    @Query("UPDATE lens SET stock_qty = stock_qty + :quantity WHERE id = :id")
    int incrementStock(@Param("id") long id, @Param("quantity") int quantity);
}
