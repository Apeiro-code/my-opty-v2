package com.myopty.shared.question.dto;

import com.myopty.shared.question.model.Question;
import java.time.LocalDateTime;

/**
 * One entry on the public FAQ.
 *
 * <p>Carries no customer id or name on purpose: the FAQ is anonymous reading, and
 * the answer is the shop's, not a private exchange — the asker's identity must not
 * leak into a payload any visitor can fetch.
 */
public record FaqResponse(long id, String questionText, String answerText, LocalDateTime answeredAt) {

    public static FaqResponse from(Question question) {
        return new FaqResponse(
                question.getId(), question.getQuestionText(), question.getAnswerText(), question.getAnsweredAt());
    }
}
