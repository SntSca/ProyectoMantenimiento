package com.esimedia.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Constantes para evitar duplicación de literales
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";
    private static final String STATUS_KEY = "status";
    private static final String DETAILS_KEY = "details";

    /**
     * Maneja errores de validación de Bean Validation en DTOs (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        
        logger.warn("Errores de validación en DTO: {}", errors);
        
        Map<String, Object> errorResponse = Map.of(
            ERROR_KEY, "Errores de validación",
            MESSAGE_KEY, "Los datos proporcionados no son válidos",
            DETAILS_KEY, errors,
            STATUS_KEY, HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja violaciones de constraints (@NotNull, @Size, etc. en parámetros individuales)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        
        logger.warn("Violaciones de constraint: {}", errors);
        
        Map<String, Object> errorResponse = Map.of(
            ERROR_KEY, "Violación de restricciones",
            MESSAGE_KEY, "Los datos no cumplen las restricciones definidas",
            DETAILS_KEY, errors,
            STATUS_KEY, HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja ResponseStatusException (errores del ValidationService y otros servicios)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        logger.warn("ResponseStatusException: {} - {}", ex.getStatusCode(), ex.getReason());
        
        Map<String, Object> errorResponse = Map.of(
            ERROR_KEY, "Error de negocio",
            MESSAGE_KEY, ex.getReason() != null ? ex.getReason() : "Error en el procesamiento",
            STATUS_KEY, ex.getStatusCode().value()
        );
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    /**
     * Maneja errores generales no capturados específicamente
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Error no manejado: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = Map.of(
            ERROR_KEY, "Error interno del servidor",
            MESSAGE_KEY, "Ha ocurrido un error inesperado",
            STATUS_KEY, HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}