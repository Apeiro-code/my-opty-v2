package com.myopty.shared.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The customer's new question.
 *
 * <p>The only field: the asker is never taken from the body. It comes from the
 * session, so one customer cannot file a question under another's name.
 */
public record CreateQuestionRequest(
        @NotBlank @Size(max = 2000) String questionText) {}
