package com.myopty.shared.question.dto;

import com.myopty.shared.question.model.Question;
import java.time.LocalDateTime;

/**
 * One of the customer's own questions, with whatever answer exists so far.
 *
 * <p>{@code answered} is a convenience flag derived from {@code answerText} being
 * present, so the client does not have to test for null to decide whether to show
 * a "waiting for the shop" note or the answer.
 */
public record QuestionResponse(
        long id,
        String questionText,
        String answerText,
        boolean answered,
        LocalDateTime askedAt,
        LocalDateTime answeredAt) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getAnswerText(),
                question.getAnswerText() != null,
                question.getAskedAt(),
                question.getAnsweredAt());
    }
}
