package com.myopty.shared.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The shop's answer to a customer's question.
 *
 * <p>The only field: the answering client is taken from the session, so the
 * service cannot be asked to credit someone else for the answer.
 */
public record AnswerQuestionRequest(
        @NotBlank(message = "Write an answer before posting.")
        @Size(max = 2000, message = "An answer must be 2000 characters or fewer.")
        String answerText) {}
