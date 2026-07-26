package org.ultra.rcrs.metadata.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebInputException;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.ConflictException;
import org.ultra.rcrs.exceptions.DecodeFromBase62Exception;
import org.ultra.rcrs.exceptions.EncodeToBase62Exception;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;
import org.ultra.rcrs.metadata.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String internalErrorMessage = "Internal Server Error";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(final NoResourceFoundException ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "resource not found"));
    }

    @ExceptionHandler(EncodeToBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleEncodeToBase62Exception(final EncodeToBase62Exception ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), internalErrorMessage));
    }

    @ExceptionHandler(DecodeFromBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleDecodeFromBase62Exception(final DecodeFromBase62Exception ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(final BadRequestException ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(final ConflictException ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(final ServiceUnavailableException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(final AuthenticationException ex) {
        log.debug("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(final AccessDeniedException ex) {
        log.debug("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Forbidden"));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(final WebExchangeBindException ex) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> String.format("%s %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }

    /**
     * {@link WebExchangeBindException} is a {@link ServerWebInputException}, so this stays below it —
     * it covers the remaining binding failures (unreadable body, wrong parameter type).
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(final ServerWebInputException ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getReason() != null ? ex.getReason() : ex.getMessage()));
    }

    /**
     * Covers the remaining exceptions that already carry an HTTP status (405, 415, …) — without this
     * they would fall through to the catch-all handler and be reported as 500.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponseException(final ErrorResponseException ex) {
        final HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        if (status.is5xxServerError()) {
            log.error(ex.getMessage(), ex);
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(status.value(), internalErrorMessage));
        }
        log.debug(ex.getMessage(), ex);
        final String detail = ex.getBody().getDetail();
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), detail != null ? detail : status.getReasonPhrase()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), internalErrorMessage));
    }
}
