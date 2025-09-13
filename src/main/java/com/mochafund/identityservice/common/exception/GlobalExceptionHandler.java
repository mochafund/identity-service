package com.mochafund.identityservice.common.exception;

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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(AccessDeniedException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Bad request")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("[{}] 403 Bad Request at {}: {}", correlationId, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header("X-Correlation-ID", correlationId)
                .body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(BadRequestException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Bad request")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("[{}] 400 Bad Request at {}: {}", correlationId, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Correlation-ID", correlationId)
                .body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ConflictException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Conflict")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("[{}] 409 Bad Request at {}: {}", correlationId, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header("X-Correlation-ID", correlationId)
                .body(body);
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
        log.warn("[{}] 404 Not Found at {}: {}", correlationId, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("X-Correlation-ID", correlationId)
                .body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(UnauthorizedException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String path = safePath(request);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Resource not found")
                .correlationId(correlationId)
                .path(path)
                .build();
        log.warn("[{}] 401 Not Found at {}: {}", correlationId, path, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("X-Correlation-ID", correlationId)
                .body(body);
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
        log.error("[{}] 500 at {}: {}", correlationId, path, ex.toString(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Correlation-ID", correlationId)
                .body(body);
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
