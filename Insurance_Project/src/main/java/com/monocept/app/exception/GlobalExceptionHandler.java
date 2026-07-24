package com.monocept.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.monocept.app.dto.ApiErrorResponseDto;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .errorType("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomExceptions.DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateResource(
            CustomExceptions.DuplicateResourceException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.CONFLICT.value())
                .errorType("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomExceptions.InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidCredentials(
            CustomExceptions.InvalidCredentialsException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .errorType("INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CustomExceptions.InactiveUserException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInactiveUser(
            CustomExceptions.InactiveUserException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .errorType("INACTIVE_USER")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CustomExceptions.PaymentProcessingException.class)
    public ResponseEntity<ApiErrorResponseDto> handlePaymentProcessing(
            CustomExceptions.PaymentProcessingException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.PAYMENT_REQUIRED.value())
                .errorType("PAYMENT_PROCESSING_FAILED")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(CustomExceptions.ClaimProcessingException.class)
    public ResponseEntity<ApiErrorResponseDto> handleClaimProcessing(
            CustomExceptions.ClaimProcessingException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorType("CLAIM_PROCESSING_FAILED")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomExceptions.PolicyExpiredException.class)
    public ResponseEntity<ApiErrorResponseDto> handlePolicyExpired(
            CustomExceptions.PolicyExpiredException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorType("POLICY_EXPIRED")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomExceptions.UnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnauthorizedAccess(
            CustomExceptions.UnauthorizedAccessException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .errorType("UNAUTHORIZED_ACCESS")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CustomExceptions.QueryAlreadyResolvedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleQueryAlreadyResolved(
            CustomExceptions.QueryAlreadyResolvedException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.CONFLICT.value())
                .errorType("QUERY_ALREADY_RESOLVED")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.monocept.app.exception.InvalidOperationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidOperation(
            com.monocept.app.exception.InvalidOperationException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorType("INVALID_OPERATION")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationFailure(
            MethodArgumentNotValidException ex, WebRequest request) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorType("VALIDATION_FAILURE")
                .message(validationErrors)
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex, WebRequest request) {
        String validationErrors = ex.getConstraintViolations().stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errorType("VALIDATION_FAILURE")
                .message(validationErrors)
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponseDto> handleOptimisticLockingFailure(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.CONFLICT.value())
                .errorType("CONCURRENT_MODIFICATION")
                .message("The resource was updated or processed by another transaction concurrently. Please refresh and try again.")
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleGenericException(
            Exception ex, WebRequest request) {
        ApiErrorResponseDto error = ApiErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorType("INTERNAL_SERVER_ERROR")
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred")
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
