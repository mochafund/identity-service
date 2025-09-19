package com.mochafund.identityservice.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Bad request")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("{} at {}: {}", HttpStatus.FORBIDDEN, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Bad request")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("{} at {}: {}", HttpStatus.BAD_REQUEST, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Conflict")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("{} at {}: {}", HttpStatus.CONFLICT ,path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Resource not found")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("{} at {}: {}", HttpStatus.NOT_FOUND ,path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Resource not found")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("{} at {}: {}",  HttpStatus.UNAUTHORIZED, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({ ConstraintViolationException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<ErrorResponse> handleConstraintViolation(Exception ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getLocalizedMessage())
                .correlationId(correlationId)
                .path(path)
                .build();
        log.error("{} at {}: {}", HttpStatus.BAD_REQUEST, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Validation failed")
                .correlationId(correlationId)
                .path(path)
                .errors(ex.getFieldErrors().stream().map(fe -> new ErrorResponse.ValidationError(fe.getField(), fe.getDefaultMessage())).toList())
                .build();
        log.error("{} at {}: {}", HttpStatus.BAD_REQUEST, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({ InternalServerException.class, Exception.class })
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("Woops, we ran into an error")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.error("{} at {}: {}", HttpStatus.INTERNAL_SERVER_ERROR, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String id = request.getHeader("X-Correlation-Id");

        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        return id;
    }

    private String safePath(HttpServletRequest request) {
        try {
            return request.getRequestURI();
        } catch (Exception e) {
            return "";
        }
    }
}

