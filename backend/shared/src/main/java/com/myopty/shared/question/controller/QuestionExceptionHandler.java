package com.myopty.shared.question.controller;

import com.myopty.shared.auth.dto.ApiError;
import com.myopty.shared.question.exception.AlreadyAnsweredException;
import com.myopty.shared.question.exception.QuestionNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns this package's failures into the repository's error envelope, the same
 * {@code { "success": false, "error": { "code", "message" } }} shape the security
 * filter chain and the other modules speak.
 *
 * <p>Scoped to {@code com.myopty.shared.question} on purpose: an unscoped advice
 * is global, and this story must not get to change how another controller reports
 * a bad request by adding a handler here.
 */
@RestControllerAdvice(basePackages = "com.myopty.shared.question")
public class QuestionExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationFailed(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" "));
        return ApiError.of("VALIDATION_FAILED", message.isEmpty() ? "The request is not valid." : message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError malformedRequest(HttpMessageNotReadableException exception) {
        return ApiError.of("MALFORMED_REQUEST", "The request body is not valid JSON.");
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError questionNotFound(QuestionNotFoundException exception) {
        return ApiError.of("QUESTION_NOT_FOUND", exception.getMessage());
    }

    /**
     * The question already has an answer. A durable state conflict, so 409 rather
     * than 400: the request was well-formed, the state refused it.
     */
    @ExceptionHandler(AlreadyAnsweredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError alreadyAnswered(AlreadyAnsweredException exception) {
        return ApiError.of("ALREADY_ANSWERED", exception.getMessage());
    }
}
