package org.ultra.rcrs.mediaservice.controller;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.ultra.rcrs.exceptions.*;
import org.ultra.rcrs.mediaservice.dto.ErrorResponse;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String internalErrorMessage = "Internal Server Error";

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

    @ExceptionHandler(ImageDecodeException.class)
    public ResponseEntity<ErrorResponse> handleImageDecodeException(final ImageDecodeException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DecodeFromBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleDecodeFromBase62Exception(final DecodeFromBase62Exception ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EncodeToBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleEncodeToBase62Exception(final EncodeToBase62Exception ex) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, internalErrorMessage);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(final ConflictException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.CONFLICT, ex.getMessage());
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

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(final HandlerMethodValidationException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, "validation failed");
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

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(final MaxUploadSizeExceededException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "uploaded file is too large");
    }

    /**
     * Covers exceptions that already carry an HTTP status (e.g. {@code UnsupportedMediaTypeStatusException}
     * thrown while validating an uploaded image) — without this they would fall through to the
     * catch-all handler and be reported as 500.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponseException(final ErrorResponseException ex) {
        log.debug(ex.getMessage(), ex);
        final HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        final String message = status.is5xxServerError() ? internalErrorMessage : ex.getBody().getDetail();
        if (status.is5xxServerError()) {
            log.error(ex.getMessage(), ex);
        }
        return error(status, message != null ? message : status.getReasonPhrase());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception ex) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, internalErrorMessage);
    }

    private ResponseEntity<ErrorResponse> error(final HttpStatus status, final String message) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status.value(), message));
    }

}
