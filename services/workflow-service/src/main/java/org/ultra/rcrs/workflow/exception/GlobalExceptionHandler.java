package org.ultra.rcrs.workflow.exception;

import io.temporal.client.WorkflowFailedException;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.ServerFailure;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.ultra.rcrs.exceptions.*;

import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Temporal serialises the original exception into an {@link ApplicationFailure} whose type is the
     * exception class name, so the status a failing activity meant to report has to be recovered by
     * that type instead of the (already lost) Java class.
     */
    private static final Map<String, HttpStatus> FAILURE_TYPE_STATUSES = Map.of(
            NotFoundException.class.getName(), HttpStatus.NOT_FOUND,
            BadRequestException.class.getName(), HttpStatus.BAD_REQUEST,
            DecodeFromBase62Exception.class.getName(), HttpStatus.BAD_REQUEST,
            ConflictException.class.getName(), HttpStatus.CONFLICT,
            ServiceUnavailableException.class.getName(), HttpStatus.SERVICE_UNAVAILABLE
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.debug("Invalid request: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.debug("Malformed request body: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, "malformed request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.debug("Invalid parameter: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, String.format("%s has invalid value", ex.getName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.debug("Missing parameter: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "resource not found");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DecodeFromBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleDecodeFromBase62Exception(DecodeFromBase62Exception ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        log.debug(ex.getMessage(), ex);
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex) {
        log.error(ex.getMessage(), ex);
        return error(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.debug("Authentication failed: {}", ex.getMessage());
        return error(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.debug("Access denied: {}", ex.getMessage());
        return error(HttpStatus.FORBIDDEN, "Forbidden");
    }

    @ExceptionHandler(WorkflowException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowException(WorkflowException ex) {
        log.error("Workflow error: {}", ex.getMessage(), ex);
        return error(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(ServerFailure.class)
    public ResponseEntity<ErrorResponse> handleTemporalServiceException(ServerFailure ex) {
        log.error("Temporal service unavailable", ex);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Workflow service unavailable: " + ex.getMessage());
    }

    /**
     * The controllers wait for the workflow result with {@code CompletableFuture#join}, so a failing
     * workflow surfaces as a {@link CompletionException} wrapping the Temporal failure chain.
     */
    @ExceptionHandler({CompletionException.class, WorkflowFailedException.class})
    public ResponseEntity<ErrorResponse> handleWorkflowExecutionException(RuntimeException ex) {
        ApplicationFailure applicationFailure = findApplicationFailure(ex);
        if (applicationFailure != null) {
            HttpStatus status = FAILURE_TYPE_STATUSES.get(applicationFailure.getType());
            if (status != null) {
                log.warn("Workflow failed with {}: {}", status, applicationFailure.getOriginalMessage());
                return error(status, applicationFailure.getOriginalMessage());
            }
        }
        log.error("Workflow execution failed", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Workflow execution failed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private static ApplicationFailure findApplicationFailure(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApplicationFailure failure) {
                return failure;
            }
        }
        return null;
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message != null ? message : status.getReasonPhrase()));
    }
}
