
package com.vivekanand.manager.common;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> bad(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> val(MethodArgumentNotValidException ex) {
        Map<String, Object> b = new HashMap<>();
        b.put("error", "Validation failed");
        b.put("details", ex.getBindingResult().getFieldErrors().stream().map(f -> Map.of("field", f.getField(), "message", f.getDefaultMessage())));
        return ResponseEntity.badRequest().body(b);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> nf(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
    }
}
