package com.myopty.billing.controller;

import com.myopty.billing.exception.InvalidPaymentException;
import com.myopty.billing.exception.InvoiceRenderingException;
import com.myopty.billing.exception.PaymentFailedException;
import com.myopty.billing.exception.PaymentGatewayException;
import com.myopty.billing.exception.PaymentNotAllowedException;
import com.myopty.billing.exception.ReceiptDeliveryException;
import com.myopty.billing.exception.RefundNotAllowedException;
import com.myopty.billing.exception.ResourceNotFoundException;
import com.myopty.shared.auth.dto.ApiError;
import java.util.stream.Collectors;
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
 * <p>Scoped to {@code com.myopty.billing}: an unscoped advice is global, and this
 * module must not get to change how another module reports a bad request. The status
 * codes separate the cases a caller acts on differently — 400 for a request the
 * server will not act on, 402 for a gateway refusal, 409 for a state that refuses the
 * action, 404 for a missing or unowned resource, and 502 for a downstream fault.
 */
@RestControllerAdvice(basePackages = "com.myopty.billing")
public class BillingExceptionHandler {

    @ExceptionHandler(InvalidPaymentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidPayment(InvalidPaymentException exception) {
        return ApiError.of("INVALID_PAYMENT", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationFailed(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" "));
        return ApiError.of("VALIDATION_FAILED", message.isEmpty() ? "The request is not valid." : message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidFilter(MethodArgumentTypeMismatchException exception) {
        return ApiError.of("INVALID_FILTER", "The filter value is not valid.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError malformedRequest(HttpMessageNotReadableException exception) {
        return ApiError.of("MALFORMED_REQUEST", "The request body is not valid JSON.");
    }

    @ExceptionHandler(PaymentFailedException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ApiError paymentFailed(PaymentFailedException exception) {
        return ApiError.of("PAYMENT_FAILED", exception.getMessage());
    }

    @ExceptionHandler(PaymentNotAllowedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError paymentNotAllowed(PaymentNotAllowedException exception) {
        return ApiError.of("PAYMENT_NOT_ALLOWED", exception.getMessage());
    }

    @ExceptionHandler(RefundNotAllowedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError refundNotAllowed(RefundNotAllowedException exception) {
        return ApiError.of("REFUND_NOT_ALLOWED", exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(ResourceNotFoundException exception) {
        return ApiError.of("NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(PaymentGatewayException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiError gateway(PaymentGatewayException exception) {
        return ApiError.of("PAYMENT_GATEWAY_FAILED", exception.getMessage());
    }

    @ExceptionHandler(ReceiptDeliveryException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiError receiptNotDelivered(ReceiptDeliveryException exception) {
        return ApiError.of("RECEIPT_NOT_DELIVERED", exception.getMessage());
    }

    @ExceptionHandler(InvoiceRenderingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError rendering(InvoiceRenderingException exception) {
        return ApiError.of("INVOICE_RENDERING_FAILED", "The invoice could not be rendered.");
    }
}
