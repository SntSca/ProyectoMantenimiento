package com.esimedia.features.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de seguridad de contraseñas que integra múltiples verificaciones.
 * Combina:
 * - PasswordDictionaryService: Verificación contra diccionario local
 * - LeakLookupService: Verificación contra brechas públicas
 */
@Service
public class PasswordSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordSecurityService.class);
    
    private final PasswordDictionaryService passwordDictionaryService;
    private final LeakLookupService leakLookupService;
    
    public PasswordSecurityService(PasswordDictionaryService passwordDictionaryService,
                                   LeakLookupService leakLookupService) {
        this.passwordDictionaryService = passwordDictionaryService;
        this.leakLookupService = leakLookupService;
    }
    
    /**
     * Verifica si una contraseña está en el diccionario de contraseñas típicas
     * 
     * @param password La contraseña a verificar
     * @return true si está en el diccionario, false si es segura
     */
    public boolean isPasswordInDictionary(String password) {
        return passwordDictionaryService.isPasswordInDictionary(password);
    }
    
    /**
     * Verifica si una contraseña ha sido comprometida en brechas públicas
     * 
     * @param password La contraseña a verificar
     * @return true si ha sido comprometida, false si está segura
     */
    public boolean isPasswordCompromised(String password) {
        return leakLookupService.isPasswordCompromised(password);
    }
    
    /**
     * Alias para isPasswordCompromised (compatible con controlador existente)
     * 
     * @param password La contraseña a verificar
     * @return true si ha sido comprometida en brechas, false si está segura
     */
    public boolean isPasswordLeaked(String password) {
        return leakLookupService.isPasswordCompromised(password);
    }
    
    /**
     * Realiza una verificación completa de seguridad de contraseña
     * Verifica contra:
     * 1. Diccionario de contraseñas típicas
     * 2. Brechas de datos públicas (si está habilitado)
     * 
     * @param password La contraseña a verificar
     * @return Resultado detallado de la verificación
     */
    public PasswordSecurityCheckResult performSecurityCheck(String password) {
        logger.debug("Realizando verificación de seguridad de contraseña");
        
        boolean inDictionary = false;
        boolean isCompromised = false;
        
        try {
            inDictionary = isPasswordInDictionary(password);
        } 
        catch (Exception e) {
            logger.error("Error al verificar diccionario: {}", e.getMessage(), e);
        }
        
        try {
            isCompromised = isPasswordCompromised(password);
        } 
        catch (Exception e) {
            logger.error("Error al verificar brechas: {}", e.getMessage(), e);
        }
        
        boolean isSecure = !inDictionary && !isCompromised;
        String message = generateSecurityMessage(inDictionary, isCompromised);
        
        return new PasswordSecurityCheckResult(
            isSecure,
            inDictionary,
            isCompromised,
            message
        );
    }
    
    /**
     * Genera un mensaje apropiado basado en los resultados de la verificación
     */
    private String generateSecurityMessage(boolean inDictionary, boolean isCompromised) {
        if (inDictionary && isCompromised) {
            return "La contraseña es muy común y ha sido comprometida en brechas de datos.";
        } 
        else if (inDictionary) {
            return "La contraseña es muy común. Por favor, elige una contraseña más fuerte.";
        } 
        else if (isCompromised) {
            return "La contraseña ha sido comprometida en una brecha de datos. Elige una diferente.";
        } 
        else {
            return "La contraseña es segura.";
        }
    }
    
    /**
     * Obtiene información sobre el estado del sistema de verificación
     */
    public PasswordSecuritySystemStatus getSystemStatus() {
        return new PasswordSecuritySystemStatus(
            passwordDictionaryService.isDictionaryLoaded(),
            passwordDictionaryService.getDictionarySize(),
            leakLookupService != null
        );
    }
    
    /**
     * Clase interna para encapsular el resultado de la verificación
     */
    public static class PasswordSecurityCheckResult {
        private final boolean isSecure;
        private final boolean inDictionary;
        private final boolean isCompromised;
        private final String message;
        
        public PasswordSecurityCheckResult(boolean isSecure, boolean inDictionary, 
                                          boolean isCompromised, String message) {
            this.isSecure = isSecure;
            this.inDictionary = inDictionary;
            this.isCompromised = isCompromised;
            this.message = message;
        }
        
        public boolean isSecure() { return isSecure; }
        public boolean isInDictionary() { return inDictionary; }
        public boolean isCompromised() { return isCompromised; }
        public String getMessage() { return message; }
    }
    
    /**
     * Clase interna para información del estado del sistema
     */
    public static class PasswordSecuritySystemStatus {
        private final boolean dictionaryLoaded;
        private final long dictionarySize;
        private final boolean leakLookupAvailable;
        
        public PasswordSecuritySystemStatus(boolean dictionaryLoaded, long dictionarySize,
                                           boolean leakLookupAvailable) {
            this.dictionaryLoaded = dictionaryLoaded;
            this.dictionarySize = dictionarySize;
            this.leakLookupAvailable = leakLookupAvailable;
        }
        
        public boolean isDictionaryLoaded() { return dictionaryLoaded; }
        public long getDictionarySize() { return dictionarySize; }
        public boolean isLeakLookupAvailable() { return leakLookupAvailable; }
    }
}
