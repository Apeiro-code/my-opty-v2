package com.myopty.shared.question.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A customer's question and, once the shop answers it, the answer shown back to
 * them and — when the shop marks it — on the public FAQ.
 *
 * <p>{@code asked_at} is {@link ReadOnlyProperty}: the database fills it from its
 * column default, and a mapped writable field would send the null the customer
 * never supplied to a {@code NOT NULL} column. The two audit columns
 * ({@code created_at}, {@code updated_at}) are not mapped at all, for the same
 * reason. {@code answeredAt} and {@code answeredBy} are writable because the
 * answering story sets them later; an unanswered question saves them null.
 */
@Table("question")
public class Question {

    @Id
    private Long id;

    private Long customerId;
    private String questionText;
    private String answerText;

    /** Named explicitly: the property is {@code faq} but the column is {@code is_faq}. */
    @Column("is_faq")
    private boolean faq;

    @ReadOnlyProperty
    private LocalDateTime askedAt;

    private LocalDateTime answeredAt;
    private Long answeredBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isFaq() {
        return faq;
    }

    public void setFaq(boolean faq) {
        this.faq = faq;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public Long getAnsweredBy() {
        return answeredBy;
    }

    public void setAnsweredBy(Long answeredBy) {
        this.answeredBy = answeredBy;
    }
}
