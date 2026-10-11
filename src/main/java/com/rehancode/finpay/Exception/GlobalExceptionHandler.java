package com.rehancode.finpay.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), message, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.warn("UsernameNotFoundException: {}", e.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessRuleFailedException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessRuleFailedException(BusinessRuleFailedException e) {
        log.warn("BusinessRuleFailedException: {}", e.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.PRECONDITION_FAILED.value(), e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(UserNotAuthenticated.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotAuthenticated(UserNotAuthenticated e) {
        log.warn("UserNotAuthenticated: {}", e.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotExists.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotExists(UserNotExists e) {
        log.warn("UserNotExists: {}", e.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientBalance(InsufficientBalanceException e) {
        log.warn("InsufficientBalanceException: {}", e.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.CONFLICT.value(), e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotAllowed.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotAllowed(UserNotAllowed e) {
        log.warn("UserNotAllowed: {}", e.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.FORBIDDEN.value(), e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DuplicateIdempotencyKeyException.class)
    public ResponseEntity<ApiResponse<Object>> HandleDuplicateIdempotencyKeyException(DuplicateIdempotencyKeyException e) {
        log.warn("DuplicateIdempotencyKeyException: {}", e.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.CONFLICT.value(), e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}