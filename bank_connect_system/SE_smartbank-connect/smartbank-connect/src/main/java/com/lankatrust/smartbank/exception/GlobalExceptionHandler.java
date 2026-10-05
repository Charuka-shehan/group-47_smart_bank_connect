package com.lankatrust.smartbank.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: You do not have permission to perform this action."));
        }
        return "redirect:/access-denied";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(NoResourceFoundException ex, Model model, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Resource not found"));
        }
        model.addAttribute("message", "The requested page was not found.");
        return "error";
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex, Model model, HttpServletRequest request) {
        log.warn("Method not supported on {}: {}", request.getRequestURI(), ex.getMessage());
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Map.of("error", "HTTP method not supported: " + ex.getMethod()));
        }
        model.addAttribute("message", "The requested HTTP method is not supported.");
        return "error";
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Object handleBusinessValidation(RuntimeException ex, Model model, HttpServletRequest request) {
        log.warn("Validation error on {}: {}", request.getRequestURI(), ex.getMessage());
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDatabaseConstraint(DataIntegrityViolationException ex, Model model, HttpServletRequest request) {
        log.error("Database constraint violation on {}: {}", request.getRequestURI(), ex.getMessage());
        String safeMessage = "A database constraint conflict occurred. The record may already exist or is referenced by another entity.";
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", safeMessage));
        }
        model.addAttribute("message", safeMessage);
        return "error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSize(MaxUploadSizeExceededException ex, Model model, HttpServletRequest request) {
        String msg = "Uploaded file exceeds the maximum allowed size.";
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        }
        model.addAttribute("message", msg);
        return "error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(MethodArgumentNotValidException ex, Model model, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errors));
        }
        model.addAttribute("message", "Validation failed: " + errors);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleAll(Exception ex, Model model, HttpServletRequest request) {
        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        String safeMessage = "An unexpected error occurred. Please contact customer support if the issue persists.";
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", safeMessage));
        }
        model.addAttribute("message", safeMessage);
        return "error";
    }
}
