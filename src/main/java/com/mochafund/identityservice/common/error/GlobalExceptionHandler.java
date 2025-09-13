package com.mochafund.identityservice.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        log.warn("[{}] 404 Not Found at {}: {}", correlationId, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.error("[{}] 500 at {}: {}", correlationId, path, ex.toString(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String id = request.getHeader("X-Correlation-Id");
        if (id == null || id.isBlank()) {
            id = request.getHeader("X-Request-Id");
        }
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
