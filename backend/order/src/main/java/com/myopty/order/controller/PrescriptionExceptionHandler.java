package com.myopty.order.controller;

import com.myopty.order.exception.InvalidDocumentException;
import com.myopty.order.exception.ObjectStoreException;
import com.myopty.shared.auth.dto.ApiError;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Turns this module's failures into the repository's error envelope, the same
 * {@code { "success": false, "error": { "code", "message" } }} shape the security
 * filter chain and the auth endpoints already speak.
 *
 * <p>Scoped to {@code com.myopty.order} on purpose: an unscoped advice is global,
 * and this module must not get to change how another module's controller reports
 * a bad request by adding a handler here.
 */
@RestControllerAdvice(basePackages = "com.myopty.order")
public class PrescriptionExceptionHandler {

    @ExceptionHandler(InvalidDocumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidDocument(InvalidDocumentException exception) {
        return ApiError.of("INVALID_DOCUMENT", exception.getMessage());
    }

    @ExceptionHandler(ObjectStoreException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError objectStoreFailure(ObjectStoreException exception) {
        return ApiError.of("DOCUMENT_STORAGE_FAILED", "The document could not be stored. Try again.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationFailed(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" "));
        return ApiError.of("VALIDATION_FAILED", message.isEmpty() ? "The prescription is not valid." : message);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError missingPrescription(MissingServletRequestPartException exception) {
        return ApiError.of("MISSING_PRESCRIPTION", "The prescription part is required.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError malformedPrescription(HttpMessageNotReadableException exception) {
        return ApiError.of("MALFORMED_PRESCRIPTION", "The prescription part is not valid JSON.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiError unsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        return ApiError.of("UNSUPPORTED_MEDIA_TYPE", "Submit prescriptions as multipart/form-data.");
    }
}
