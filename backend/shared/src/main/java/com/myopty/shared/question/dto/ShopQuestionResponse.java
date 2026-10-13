package com.myopty.shared.question.dto;

import com.myopty.shared.question.model.Question;
import java.time.LocalDateTime;

/**
 * One unanswered question in the shop's queue, with who asked it.
 *
 * <p>Unlike the public {@link FaqResponse}, this carries the asker's name and
 * email: the shop is answering a named customer, not publishing an anonymous
 * entry, and it needs a way to know whose question is waiting. The read is only
 * reachable through {@code /api/shop/**}, so the identity is not exposed to a
 * visitor.
 */
public record ShopQuestionResponse(
        long id, String customerName, String customerEmail, String questionText, LocalDateTime askedAt) {

    public static ShopQuestionResponse from(Question question, String customerName, String customerEmail) {
        return new ShopQuestionResponse(
                question.getId(), customerName, customerEmail, question.getQuestionText(), question.getAskedAt());
    }
}
