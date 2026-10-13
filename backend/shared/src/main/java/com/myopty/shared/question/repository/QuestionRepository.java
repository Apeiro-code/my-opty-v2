package com.myopty.shared.question.repository;

import com.myopty.shared.question.model.Question;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Persists and reads {@code question}.
 *
 * <p>A narrow marker repository rather than {@code CrudRepository}: the story
 * creates a question and reads a few lists back. The lists are explicit
 * {@code @Query} strings rather than derived names because each carries an
 * ordering the method name would spell out less clearly, and because the FAQ read
 * has to name the {@code is_faq} column directly — the derived-property route
 * would depend on how the boolean field's column is inferred.
 */
public interface QuestionRepository extends Repository<Question, Long> {

    Question save(Question question);

    Optional<Question> findById(Long id);

    /** A customer's own questions, newest first, answered or not. */
    @Query("""
            SELECT * FROM question
            WHERE customer_id = :customerId
            ORDER BY asked_at DESC, id DESC
            """)
    List<Question> findForCustomer(@Param("customerId") Long customerId);

    /**
     * The shop's answering queue: questions nobody has answered yet, oldest first
     * so the longest-waiting customer is served first.
     */
    @Query("""
            SELECT * FROM question
            WHERE answer_text IS NULL
            ORDER BY asked_at ASC, id ASC
            """)
    List<Question> findUnanswered();

    /**
     * The public FAQ: answered questions the shop marked as generally useful.
     * Ordered newest answer first, which is the order a reader scans them in.
     */
    @Query("""
            SELECT * FROM question
            WHERE is_faq = TRUE AND answer_text IS NOT NULL
            ORDER BY answered_at DESC, id DESC
            """)
    List<Question> findAnsweredFaqs();
}
