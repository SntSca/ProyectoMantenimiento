package com.esimedia.features.auth.services;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.enums.Rol;

/**
 * Servicio para gestionar el timeout absoluto de sesiones de usuario.
 * Controla el tiempo máximo de sesión desde el login, independientemente de la actividad.
 */
@Service
public class SessionTimeoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionTimeoutService.class);
    
    // Tiempos de absolute timeout en milisegundos
    // 8 horas para usuarios normales
    private static final long ABSOLUTE_TIMEOUT_NORMAL = 8L * 60 * 60 * 1000; 
    // 6 horas para creadores
    private static final long ABSOLUTE_TIMEOUT_CREADOR = 6L * 60 * 60 * 1000; 
    // 6 horas para administradores
    private static final long ABSOLUTE_TIMEOUT_ADMIN = 6L * 60 * 60 * 1000; 
    
    // Almacena las sesiones activas: userId -> SessionInfo
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Registra una nueva sesión cuando el usuario hace login
     * @param userId ID del usuario
     * @param rol Rol del usuario
     */
    public void registerSession(String userId, Rol rol) {
        long loginTime = System.currentTimeMillis();
        long absoluteExpirationTime = loginTime + getAbsoluteTimeoutByRole(rol);
        
        SessionInfo sessionInfo = new SessionInfo(userId, rol, loginTime, absoluteExpirationTime);
        activeSessions.put(userId, sessionInfo);
        
        logger.info("Sesión registrada para usuario {} con rol {}. Expira en {} horas.", 
                   userId, rol, getAbsoluteTimeoutByRole(rol) / (60 * 60 * 1000));
    }
    
    /**
     * Valida si la sesión del usuario sigue siendo válida (no ha expirado el tiempo absoluto)
     * @param userId ID del usuario
     * @return true si la sesión es válida, false si ha expirado
     */
    public boolean isSessionValid(String userId) {
        SessionInfo sessionInfo = activeSessions.get(userId);
        
        if (sessionInfo == null) {
            logger.warn("No se encontró sesión activa para el usuario {}", userId);
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime > sessionInfo.absoluteExpirationTime) {
            logger.info("Sesión expirada para usuario {}. Tiempo límite alcanzado.", userId);
            invalidateSession(userId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Invalida/elimina la sesión de un usuario (logout)
     * @param userId ID del usuario
     */
    public void invalidateSession(String userId) {
        SessionInfo removed = activeSessions.remove(userId);
        if (removed != null) {
            logger.info("Sesión invalidada para usuario {} con rol {}", userId, removed.rol);
        }
    }
    
    /**
     * Obtiene el tiempo restante de sesión en milisegundos
     * @param userId ID del usuario
     * @return Tiempo restante en ms, o -1 si no hay sesión o está expirada
     */
    public long getRemainingSessionTime(String userId) {
        SessionInfo sessionInfo = activeSessions.get(userId);
        
        if (sessionInfo == null) {
            return -1;
        }
        
        long currentTime = System.currentTimeMillis();
        long remaining = sessionInfo.absoluteExpirationTime - currentTime;
        
        return remaining > 0 ? remaining : -1;
    }
    
    /**
     * Obtiene información de la sesión del usuario
     * @param userId ID del usuario
     * @return SessionInfo o null si no existe
     */
    public SessionInfo getSessionInfo(String userId) {
        return activeSessions.get(userId);
    }
    
    /**
     * Limpieza periódica de sesiones expiradas (cada 15 minutos)
     */
    // 15 minutos
    @Scheduled(fixedRate = 15 * 60 * 1000) 
    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        int expiredCount = 0;
        
        for (Map.Entry<String, SessionInfo> entry : activeSessions.entrySet()) {
            if (currentTime > entry.getValue().absoluteExpirationTime) {
                activeSessions.remove(entry.getKey());
                expiredCount++;
                logger.debug("Sesión expirada eliminada para usuario {}", entry.getKey());
            }
        }
        
        if (expiredCount > 0) {
            logger.info("Limpieza de sesiones: {} sesiones expiradas eliminadas", expiredCount);
        }
    }
    
    /**
     * Obtiene el tiempo de absolute timeout según el rol del usuario
     * @param rol Rol del usuario
     * @return Tiempo de absolute timeout en milisegundos
     */
    private long getAbsoluteTimeoutByRole(Rol rol) {
        switch (rol) {
            case CREADOR:
                // 6 horas
                return ABSOLUTE_TIMEOUT_CREADOR; 
            case ADMINISTRADOR:
                // 6 horas
                return ABSOLUTE_TIMEOUT_ADMIN; 
            case NORMAL:
            default:
                // 8 horas
                return ABSOLUTE_TIMEOUT_NORMAL; 
        }
    }
    
    /**
     * Obtiene el número de sesiones activas
     * @return Número de sesiones activas
     */
    public int getActiveSessionsCount() {
        return activeSessions.size();
    }
    
    /**
     * Clase interna para almacenar información de la sesión
     */
    public static class SessionInfo {
        private final String userId;
        private final Rol rol;
        private final long loginTime;
        private final long absoluteExpirationTime;
        
        public SessionInfo(String userId, Rol rol, long loginTime, long absoluteExpirationTime) {
            this.userId = userId;
            this.rol = rol;
            this.loginTime = loginTime;
            this.absoluteExpirationTime = absoluteExpirationTime;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public Rol getRol() {
            return rol;
        }
        
        public long getLoginTime() {
            return loginTime;
        }
        
        public long getAbsoluteExpirationTime() {
            return absoluteExpirationTime;
        }
        
        public Instant getLoginInstant() {
            return Instant.ofEpochMilli(loginTime);
        }
        
        public Instant getExpirationInstant() {
            return Instant.ofEpochMilli(absoluteExpirationTime);
        }
    }
}
