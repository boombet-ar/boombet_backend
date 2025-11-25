package com.boombet.boombet_backend.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CASO 1: Error validando la Entidad (JPA/Hibernate)
    // Este es el error que te está saliendo ahora mismo
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleJPAViolations(ConstraintViolationException e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errores = new HashMap<>();

        // Recorremos los errores para sacar solo campo y mensaje
        e.getConstraintViolations().forEach(violation -> {
            errores.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        response.put("status", "error");
        response.put("mensaje", "Error de validación en base de datos");
        response.put("detalles", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleDTOViolations(MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errores = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        response.put("status", "error");
        response.put("mensaje", "Datos de entrada inválidos");
        response.put("detalles", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}