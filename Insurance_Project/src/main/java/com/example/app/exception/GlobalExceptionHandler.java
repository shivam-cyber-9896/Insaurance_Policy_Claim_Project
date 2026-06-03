package com.example.app.exception;

import com.example.app.dto.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomExceptions.ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleResourceNotFound(
            CustomExceptions.ResourceNotFoundException ex, WebRequest request) {
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
