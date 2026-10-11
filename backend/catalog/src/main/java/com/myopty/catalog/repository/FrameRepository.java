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

    List<Frame> findAllByOrderByIdAsc();

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
