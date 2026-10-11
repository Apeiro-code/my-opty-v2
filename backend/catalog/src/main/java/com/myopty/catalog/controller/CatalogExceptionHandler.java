package com.myopty.catalog.controller;

import com.myopty.catalog.exception.InvalidCategoryException;
import com.myopty.catalog.exception.ReportExportException;
import com.myopty.catalog.exception.ResourceNotFoundException;
import com.myopty.shared.auth.dto.ApiError;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns this module's failures into the repository's error envelope, the same
 * {@code { "success": false, "error": { "code", "message" } }} shape the security
 * filter chain and the auth endpoints already speak.
 *
 * <p>Scoped to {@code com.myopty.catalog} on purpose: an unscoped advice is
 * global, and this module must not get to change how another module's controller
 * reports a bad request by adding a handler here.
 */
@RestControllerAdvice(basePackages = "com.myopty.catalog")
public class CatalogExceptionHandler {

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

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(ResourceNotFoundException exception) {
        return ApiError.of("NOT_FOUND", exception.getMessage());
    }

    /**
     * A frame or lens was filed under a category it cannot belong to. The id is a
     * client mistake, so it answers 400 rather than 404 — the address that was
     * wrong is the request body, not the path.
     */
    @ExceptionHandler(InvalidCategoryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidCategory(InvalidCategoryException exception) {
        return ApiError.of("INVALID_CATEGORY", exception.getMessage());
    }

    /**
     * A path or query value could not be converted to the type the handler wants
     * — an unknown {@code ?format=} on the report export is the case this is here
     * for. It is a client mistake, so it answers 400 in the same envelope as the
     * rest of the module's bad requests rather than Spring's default body.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError parameterMismatch(MethodArgumentTypeMismatchException exception) {
        return ApiError.of("INVALID_PARAMETER", "The value of '" + exception.getName() + "' is not valid.");
    }

    /**
     * A foreign key or {@code CHECK} constraint refused the write. A category id
     * that does not exist is the case this is here for; it is a client mistake,
     * not a server one, so it answers 400 rather than the default 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError constraintViolated(DataIntegrityViolationException exception) {
        return ApiError.of(
                "INVALID_RECORD", "The record could not be saved: a referenced or constrained value is not valid.");
    }

    /**
     * The report could not be rendered as a file. The request was valid, so this
     * is the server's failure to produce the document and answers 500.
     */
    @ExceptionHandler(ReportExportException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError exportFailed(ReportExportException exception) {
        return ApiError.of("EXPORT_FAILED", exception.getMessage());
    }
}
