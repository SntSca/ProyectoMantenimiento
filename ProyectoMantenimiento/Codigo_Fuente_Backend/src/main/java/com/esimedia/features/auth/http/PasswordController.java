package com.esimedia.features.auth.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.services.LoginAttemptService;
import com.esimedia.features.user_management.services.PasswordManagementService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users/password")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class PasswordController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);
    
    // Constantes para literales utilizados frecuentemente
    private static final String EMAIL_FIELD = "email";
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String SEGUNDOS_LITERAL = "segundos";
    private static final String TOKEN_FIELD = "token";
    private static final String FROM_IP_LITERAL = " desde IP: ";
    private static final String DEMASIADOS_INTENTOS_IP = "Demasiados intentos de registro desde esta IP. Inténtalo de nuevo en ";
    private static final String VALID_KEY = "valid";

    private final PasswordManagementService passwordManagementService;
    private final LoginAttemptService attemptService;

    public PasswordController(PasswordManagementService passwordManagementService,
                             LoginAttemptService attemptService) {
        this.passwordManagementService = passwordManagementService;
        this.attemptService = attemptService;
    }

    // Endpoints para recuperación de contraseña
    @PostMapping("/forgot")
    public ResponseEntity<Object> forgotPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get(EMAIL_FIELD);
        String ip = request.getRemoteAddr();
        logger.info("Solicitud de recuperación de contraseña para email: {} desde IP: {}", email, ip);

        // Rate limiting por IP para recuperación de contraseña
        if (attemptService.isIpBlocked(ip)) {
            long retryAfter = attemptService.getIpRetryAfterSeconds(ip);
            logger.warn("[FORGOT-PASSWORD] IP bloqueada: {}. Retry-After: {}", ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body("Demasiados intentos de recuperación desde esta IP. Inténtalo de nuevo en " + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }

        try {
            // Permite que el frontend indique el tipo de usuario si lo desea, por defecto "normal"
            String userType = payload.getOrDefault("userType", "normal");
            String result = this.passwordManagementService.requestPasswordResetGeneric(email, userType);
            return ResponseEntity.ok(result);
        }
        catch (Exception e) {
            attemptService.recordFailedAttempt("forgot-" + ip, ip);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al procesar solicitud de recuperación de contraseña para email: " + email + FROM_IP_LITERAL + ip, e);
        }
    }

    // Endpoint para recuperación de contraseña de usuarios privilegiados (creador o admin)
    @PostMapping("/forgot-privileged")
    public ResponseEntity<Object> forgotPasswordPrivileged(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get(EMAIL_FIELD);
        String ip = request.getRemoteAddr();
        logger.info("Solicitud de recuperación de contraseña PRIVILEGIADO para email: {} desde IP: {}", email, ip);

        // Rate limiting por IP para recuperación de contraseña privilegiada
        if (attemptService.isIpBlocked(ip)) {
            long retryAfter = attemptService.getIpRetryAfterSeconds(ip);
            logger.warn("[FORGOT-PASSWORD-PRIVILEGED] IP bloqueada: {}. Retry-After: {}", ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body("Demasiados intentos de recuperación desde esta IP. Inténtalo de nuevo en " + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }

        try {
            String result = passwordManagementService.requestPasswordResetGeneric(email, "privileged");
            return ResponseEntity.ok(result);
        }
        catch (Exception e) {
            attemptService.recordFailedAttempt("forgot-priv-" + ip, ip);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al procesar solicitud de recuperación de contraseña privilegiada para email: " + email + FROM_IP_LITERAL + ip, e);
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String token = payload.get(TOKEN_FIELD);
        String ip = request.getRemoteAddr();
        logger.info("Solicitud de restablecimiento de contraseña con token desde IP: {}", ip);

        if (attemptService.isIpBlocked(ip)) {
            long retryAfter = attemptService.getIpRetryAfterSeconds(ip);
            logger.warn("[RESET-PASSWORD] IP bloqueada: {}. Retry-After: {}", ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body(DEMASIADOS_INTENTOS_IP + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }

        try {
            String result = this.passwordManagementService.resetPassword(token, payload.get("newPassword"));
            return ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            attemptService.recordFailedAttempt("reset-" + ip, ip);
            // Propagamos el mismo código y mensaje (400, 404, etc.)
            throw e;
        }
        catch (Exception e) {
            attemptService.recordFailedAttempt("reset-" + ip, ip);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error al procesar restablecimiento de contraseña desde IP: " + ip, e);
        }
    }


    @GetMapping("/validate-reset-token/{token}")
    public Map<String, Boolean> validateResetToken(@PathVariable("token") String token) {
        logger.info("Validando token de recuperación");
        boolean isValid = this.passwordManagementService.validateResetToken(token);
        return Map.of(VALID_KEY, isValid);
    }
}
