package org.ultra.rcrs.catalogservice.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.ultra.rcrs.catalogservice.dto.ErrorResponse;
import org.ultra.rcrs.exceptions.DecodeFromBase62Exception;
import org.ultra.rcrs.exceptions.EncodeToBase62Exception;
import org.ultra.rcrs.exceptions.NotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String internalErrorMessage = "Internal Server Error";

    @ExceptionHandler(produces = "application/json", exception = NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Object not found"));
    }

    @ExceptionHandler(produces = "application/json", exception = EncodeToBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleEncodeToBase62Exception(final EncodeToBase62Exception ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), internalErrorMessage));
    }

    @ExceptionHandler(produces = "application/json", exception = DecodeFromBase62Exception.class)
    public ResponseEntity<ErrorResponse> handleDecodeFromBase62Exception(final DecodeFromBase62Exception ex) {
        log.debug(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(produces = "application/json", exception = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), internalErrorMessage));
    }

    @ExceptionHandler(produces = "application/json", exception = WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(final WebExchangeBindException ex) {

        final BindingResult bindingResult = ex.getBindingResult();
        final String message = bindingResult.getFieldErrors().stream().
                findFirst()
                .map(fieldError -> String.format("%s %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .orElse(null);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }


}