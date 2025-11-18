package com.timetable.timetable.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.timetable.timetable.common.response.ErrorResponse;
import com.timetable.timetable.common.response.ResponseFactory;
import com.timetable.timetable.domain.user.exception.UserNotAuthorizedException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ================================
    // Validation Errors (400)
    // ================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));

        log.warn("Validation failed for {} {}: {}", request.getMethod(), request.getRequestURI(), errors);
        return ResponseFactory.error(HttpStatus.BAD_REQUEST, errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String message = "Database constraint violation: " + ex.getConstraintName();
        log.warn("Constraint violation on {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseFactory.error(HttpStatus.BAD_REQUEST, message, request);
    }

    // ================================
    // Authorization (401/403)
    // ================================

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthorized(
        UserNotAuthorizedException ex,
        HttpServletRequest request) {
        log.warn("Unauthorized: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseFactory.error(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
        AuthorizationDeniedException ex,
        HttpServletRequest request) {
        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseFactory.error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // ================================
    // Not Found (404)
    // ================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        log.warn("404 Resource Not Found: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseFactory.error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        NoHandlerFoundException ex,
        HttpServletRequest request) {
        log.warn("404 Not Found: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseFactory.error(HttpStatus.NOT_FOUND, "The requested resource was not found", request);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleEmptyResult(
            EmptyResultDataAccessException ex,
            HttpServletRequest request) {
        log.warn("404 Not Found: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseFactory.error(HttpStatus.NOT_FOUND, "Resource not found", request);
    }

    // ================================
    // Bad Request (400)
    // ================================

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception ex,
            HttpServletRequest request) {

        String message;

        if (ex instanceof HttpMessageNotReadableException) {
            message = "Malformed JSON or invalid request body";
        } else if (ex instanceof MethodArgumentTypeMismatchException mae) {
            message = String.format("Invalid value for parameter '%s': expected type '%s'",
                    mae.getName(), mae.getRequiredType() != null ? mae.getRequiredType().getSimpleName() : "unknown");
        } else if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        } else {
            message = "Bad request";
        }

        log.warn("400 Bad Request: {} {} - {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseFactory.error(HttpStatus.BAD_REQUEST, message, request);
    }

    // ================================
    // Conflict (409)
    // ================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String message = "Database integrity violation";
        if (ex.getCause() != null) {
            message += ": " + ex.getCause().getMessage();
        }

        log.warn("409 Conflict on {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseFactory.error(HttpStatus.CONFLICT, message, request);
    }

    // ================================
    // Method Not Allowed (405)
    // ================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request) {
        log.warn("405 Method Not Allowed: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseFactory.error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    // ================================
    // Internal Server Error (500)
    // ================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request) {
        log.error("500 Internal Server Error on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseFactory.internalError(ex, request);
    }

    // ================================
    // Utility Methods
    // ================================

    private String formatFieldError(FieldError error) {
        return String.format("Field '%s' %s", error.getField(), error.getDefaultMessage());
    }
}
