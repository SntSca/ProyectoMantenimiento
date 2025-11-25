package com.esimedia.features.auth.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esimedia.features.auth.services.SessionTimeoutService;
import com.esimedia.shared.util.JwtUtil;

/**
 * Controlador para consultar información de sesiones y timeouts
 */
@RestController
@RequestMapping("/users/session")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class SessionInfoController {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionInfoController.class);
    private static final String MESSAGE_KEY = "message";
    
    private final SessionTimeoutService sessionTimeoutService;
    private final JwtUtil jwtUtil;
    
    public SessionInfoController(SessionTimeoutService sessionTimeoutService, JwtUtil jwtUtil) {
        this.sessionTimeoutService = sessionTimeoutService;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Obtiene información sobre la sesión actual del usuario
     * @param authHeader Token JWT del usuario
     * @return Información de la sesión incluyendo tiempo restante
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = jwtUtil.getUserIdFromToken(authHeader);
            
            SessionTimeoutService.SessionInfo sessionInfo = sessionTimeoutService.getSessionInfo(userId);
            
            if (sessionInfo == null) {
                logger.warn("[SESSION-INFO] No se encontró sesión para usuario: {}", userId);
                return ResponseEntity.ok(Map.of(
                    "sessionActive", false,
                    MESSAGE_KEY, "No hay sesión activa registrada"
                ));
            }
            
            long remainingMs = sessionTimeoutService.getRemainingSessionTime(userId);
            boolean isValid = sessionTimeoutService.isSessionValid(userId);
            
            // Convertir a formatos legibles
            long remainingSeconds = remainingMs / 1000;
            long remainingMinutes = remainingSeconds / 60;
            long remainingHours = remainingMinutes / 60;
            
            logger.info("[SESSION-INFO] Usuario {} - Tiempo restante: {}h {}m {}s", 
                       userId, 
                       remainingHours, 
                       remainingMinutes % 60, 
                       remainingSeconds % 60);
            
            return ResponseEntity.ok(Map.of(
                "sessionActive", isValid,
                "userId", sessionInfo.getUserId(),
                "rol", sessionInfo.getRol().toString(),
                "loginTime", sessionInfo.getLoginInstant().toString(),
                "expirationTime", sessionInfo.getExpirationInstant().toString(),
                "remainingMilliseconds", remainingMs,
                "remainingSeconds", remainingSeconds,
                "remainingMinutes", remainingMinutes,
                "remainingHours", remainingHours,
                "remainingFormatted", String.format("%d horas, %d minutos, %d segundos", 
                                                    remainingHours, 
                                                    remainingMinutes % 60, 
                                                    remainingSeconds % 60)
            ));
            
        } 
        catch (Exception e) {
            logger.error("[SESSION-INFO] Error al obtener información de sesión", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al obtener información de sesión",
                MESSAGE_KEY, e.getMessage()
            ));
        }
    }
    
    /**
     * Obtiene estadísticas generales de sesiones activas
     * @return Número de sesiones activas
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats() {
        int activeSessionsCount = sessionTimeoutService.getActiveSessionsCount();
        
        logger.info("[SESSION-STATS] Sesiones activas: {}", activeSessionsCount);
        
        return ResponseEntity.ok(Map.of(
            "activeSessionsCount", activeSessionsCount,
            MESSAGE_KEY, "Estadísticas de sesiones activas"
        ));
    }
}
