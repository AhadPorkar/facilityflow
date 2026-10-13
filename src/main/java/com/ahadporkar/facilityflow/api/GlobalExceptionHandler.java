package com.ahadporkar.facilityflow.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Map<String,Object>> failure(HttpStatus status,String code,String message) {
        return ResponseEntity.status(status).body(Map.of(
            "code", code, "message", message, "timestamp", Instant.now().toString()));
    }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String,Object>> domain(ApiException ex) {
        return failure(ex.getStatus(),ex.getCode(),ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException ex) {
        return failure(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR",
            ex.getBindingResult().getFieldErrors().stream()
              .map(e -> e.getField()+": "+e.getDefaultMessage()).findFirst().orElse("Invalid input"));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Map<String,Object>> constraint(ConstraintViolationException ex) {
        return failure(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Invalid request parameter");
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String,Object>> integrity(DataIntegrityViolationException ex) {
        return failure(HttpStatus.CONFLICT,"DUPLICATE_OR_REFERENCE","Duplicate identifier or referenced record");
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String,Object>> denied(AccessDeniedException ex) {
        return failure(HttpStatus.FORBIDDEN,"ACCESS_DENIED","Operation not permitted");
    }
}
