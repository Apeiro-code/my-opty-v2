package com.myopty.catalog.repository;

import com.myopty.catalog.model.Frame;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

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
 */
public interface FrameRepository extends Repository<Frame, Long> {

    Frame save(Frame frame);

    Optional<Frame> findById(Long id);

    List<Frame> findAllByOrderByIdAsc();
}
