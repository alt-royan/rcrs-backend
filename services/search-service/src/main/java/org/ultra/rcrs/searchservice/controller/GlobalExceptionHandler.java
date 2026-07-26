package org.ultra.rcrs.searchservice.controller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.DecodeFromBase62Exception;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;
import org.ultra.rcrs.searchservice.dto.ErrorResponse;

import java.util.concurrent.CompletionException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INTERNAL_ERROR_MESSAGE = "Internal Server Error";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(final NoResourceFoundException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.NOT_FOUND, "resource not found");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(final BadRequestException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DecodeFromBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleDecodeFromBase62Exception(final DecodeFromBase62Exception ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(final ServiceUnavailableException ex) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(final AuthenticationException ex) {
        log.debug("Authentication failed: {}", ex.getMessage());
        return error(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(final AccessDeniedException ex) {
        log.debug("Access denied: {}", ex.getMessage());
        return error(HttpStatus.FORBIDDEN, "Forbidden");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        final BindingResult bindingResult = ex.getBindingResult();
        final String message = bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> String.format("%s %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .orElse("validation failed");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(final ConstraintViolationException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, String.format("%s has invalid value", ex.getName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(final MissingServletRequestParameterException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, "malformed request body");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    /**
     * The per-type searches run in parallel, so a failure surfaces wrapped in a
     * {@link CompletionException} — unwrap it to keep the status of the original exception.
     */
    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ErrorResponse> handleCompletionException(final CompletionException ex) {
        return switch (ex.getCause()) {
            case NotFoundException cause -> handleNotFoundException(cause);
            case BadRequestException cause -> handleBadRequestException(cause);
            case DecodeFromBase62Exception cause -> handleDecodeFromBase62Exception(cause);
            case ServiceUnavailableException cause -> handleServiceUnavailableException(cause);
            case null, default -> {
                log.error(ex.getMessage(), ex);
                yield error(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE);
            }
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception ex) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE);
    }

    private static ResponseEntity<ErrorResponse> error(final HttpStatus status, final String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message != null ? message : status.getReasonPhrase()));
    }
}
