package com.myopty.catalog.repository;

import com.myopty.catalog.model.Frame;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Writes and reads {@code frame}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: this story
 * creates a frame, edits one and lists them for the shop. The customer-facing
 * browsing story will widen it deliberately with the filtered queries it needs,
 * rather than inheriting a dozen methods this module does not use yet.
 *
 * <p>{@link #findAllByOrderByIdAsc} is unscoped by design — every caller is the
 * shop owner, because the controller sits behind {@code /api/shop/**} and the
 * filter chain has already required the client role.
 *
 * <p>Stock moves are single conditional {@code UPDATE}s rather than
 * read-modify-write through the entity, so two orders for the same frame cannot
 * decrement each other's view of {@code stock_qty}: the {@code ... AND stock_qty
 * >= :quantity} guard is what lets a scarce shelf refuse the second order instead
 * of letting it go negative.
 */
public interface FrameRepository extends Repository<Frame, Long> {

    Frame save(Frame frame);

    Optional<Frame> findById(Long id);

    Optional<Frame> findByIdAndActiveTrue(Long id);

    List<Frame> findAllByOrderByIdAsc();

    /**
     * The frames on the shop wall, oldest first, for a browsing visitor. Scoped to
     * {@code is_active = true} because an inactive frame is kept only so old orders
     * resolve and must never appear on the storefront; the wrap in the service is
     * what narrows it to the public fields.
     */
    List<Frame> findAllByActiveTrueOrderByIdAsc();

    /**
     * The frames on the shop wall, filtered for a browsing visitor. All filters are
     * optional: a null parameter means "no filter". The results are oldest first and
     * only active frames are returned.
     */
    @Query("""
            SELECT f.* FROM frame f
            LEFT JOIN category c ON f.category_id = c.id
            WHERE f.is_active = TRUE
              AND (:categorySlug IS NULL OR LOWER(c.slug) = LOWER(:categorySlug))
              AND (:minPrice IS NULL OR f.price >= :minPrice)
              AND (:maxPrice IS NULL OR f.price <= :maxPrice)
              AND (:color IS NULL OR f.color IS NULL OR LOWER(f.color) LIKE LOWER(CONCAT('%', :color, '%')))
              AND (:material IS NULL OR LOWER(f.material) LIKE LOWER(CONCAT('%', :material, '%')))
              AND (:q IS NULL OR LOWER(f.model) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY f.id ASC
            """)
    List<Frame> findActiveForStorefront(
            @Param("categorySlug") String categorySlug,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("color") String color,
            @Param("material") String material,
            @Param("q") String q);

    /**
     * Takes {@code quantity} off a frame in one statement. Returns the number of
     * rows changed, which is zero — and nothing is modified — when the frame
     * does not have that many left.
     */
    @Modifying
    @Query("UPDATE frame SET stock_qty = stock_qty - :quantity WHERE id = :id AND stock_qty >= :quantity")
    int decrementStock(@Param("id") long id, @Param("quantity") int quantity);

    /** Puts {@code quantity} back on a frame. Returns the number of rows changed. */
    @Modifying
    @Query("UPDATE frame SET stock_qty = stock_qty + :quantity WHERE id = :id")
    int incrementStock(@Param("id") long id, @Param("quantity") int quantity);
}
