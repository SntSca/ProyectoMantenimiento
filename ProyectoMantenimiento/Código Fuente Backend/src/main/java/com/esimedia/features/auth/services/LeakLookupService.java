package com.esimedia.features.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Servicio para verificar contraseñas contra brechas de datos usando Leak-Lookup.
 * Este servicio verifica internamente si una contraseña ha sido comprometida en breaches públicos.
 * 
 * Implementa seguridad mediante k-anonymity: solo se envían los primeros 5 caracteres del hash SHA-1
 * a la API, reduciendo el riesgo de exposición de la contraseña.
 * 
 * Integración con Leak-Lookup API: https://leak-lookup.com/api
 * 
 * El usuario no verá directamente esta validación, solo recibirá un mensaje si la contraseña
 * ha sido comprometida.
 */
@Service
public class LeakLookupService {

    private static final Logger logger = LoggerFactory.getLogger(LeakLookupService.class);
    
    // Configuración de la API Leak-Lookup
    private static final String LEAK_LOOKUP_API_URL = "https://leak-lookup.com/api";
    private static final String HASHES_KEY = "hashes";
    
    @Value("${leak-lookup.api-key:5a1e5e8f4470da68b2fad056f72ba737}")
    private String leakLookupApiKey;
    
    @Value("${leak-lookup.enabled:true}")
    private boolean enableLeakCheck;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public LeakLookupService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Verifica si una contraseña ha sido comprometida en alguna brecha de datos.
     * Implementa k-anonymity: solo envía los primeros 5 caracteres del hash SHA-1.
     * 
     * IMPORTANTE: El usuario NUNCA recibe la contraseña en texto plano en la respuesta.
     * Solo se le informa de que la contraseña ha sido comprometida.
     * 
     * @param password La contraseña a verificar (nunca se envía al servidor externo en texto plano)
     * @return true si la contraseña ha sido comprometida, false en caso contrario
     */
    public boolean isPasswordCompromised(String password) {
        boolean compromised = false;
        
        // Si el servicio está deshabilitado, no hacer la verificación
        if (!enableLeakCheck) {
            logger.debug("Leak-Lookup check deshabilitado. Omitiendo verificación de brechas.");
        } 
        else if (password == null || password.isEmpty()) {
            // No verificar contraseñas nulas o vacías
        } 
        else {
            try {
                logger.debug("Verificando contraseña contra brechas de datos con Leak-Lookup...");
                compromised = checkWithLeakLookupAPI(password);
                
                if (compromised) {
                    logger.warn("Contraseña comprometida detectada durante registro/cambio de contraseña");
                }
            } 
        catch (Exception e) {
                logger.error("Error al verificar contraseña contra brechas: {}", e.getMessage(), e);
                // En caso de error, permitir el registro para no bloquear al usuario
                // compromised remains false
            }
        }
        
        return compromised;
    }
    
    /**
     * Verifica una contraseña de manera segura contra Leak-Lookup con k-anonymity.
     * 
     * Proceso:
     * 1. Calcula SHA-1 de la contraseña
     * 2. Envía solo los primeros 5 caracteres a la API (k-anonymity)
     * 3. Recibe lista de hashes parciales comprometidos
     * 4. Verifica si el hash completo coincide con alguno en la lista
     */
    private boolean checkWithLeakLookupAPI(String password) throws Exception {
        // 1. Calcular SHA-1 hash de la contraseña
        String passwordHash = computeSHA1(password);
        String hashPrefix = passwordHash.substring(0, Math.min(5, passwordHash.length())).toUpperCase();
        String hashSuffix = passwordHash.substring(5).toUpperCase();
        
        logger.debug("Hash prefix enviado a API: {}", hashPrefix);
        
        try {
            // 2. Llamar a Leak-Lookup API con el prefijo (k-anonymity)
            String apiUrl = LEAK_LOOKUP_API_URL + "?key=" + leakLookupApiKey + "&hash=" + hashPrefix;
            
            logger.debug("Llamando a Leak-Lookup API: {}", apiUrl.replace(leakLookupApiKey, "***"));
            
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            
            // 3. Verificar respuesta exitosa
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                logger.warn("Respuesta inesperada de Leak-Lookup API: {}", response.getStatusCode());
                return false;
            }
            
            // 4. Parsear respuesta JSON y verificar hashes
            return checkHashesInResponse(response.getBody(), hashSuffix);
            
        } 
        catch (RestClientException e) {
            logger.error("Error de conectividad con Leak-Lookup API: {}", e.getMessage(), e);
            // Permitir registro si la API no está disponible
            return false;
        }
    }
    
    /**
     * Verifica si el hash de la contraseña está en la lista de hashes comprometidos.
     */
    private boolean checkHashesInResponse(String responseBody, String hashSuffix) throws Exception {
        JsonNode responseNode = objectMapper.readTree(responseBody);
        
        // Verificar que existe el array de hashes
        if (!responseNode.has(HASHES_KEY) || !responseNode.get(HASHES_KEY).isArray()) {
            logger.debug("Contraseña no encontrada en brechas de Leak-Lookup");
            return false;
        }
        
        // Buscar el hash en la lista de comprometidos
        for (JsonNode hashNode : responseNode.get(HASHES_KEY)) {
            String compromisedHash = hashNode.asText().toUpperCase();
            
            // Comparar solo el sufijo (la API devuelve el resto del hash después del prefijo)
            if (compromisedHash.equalsIgnoreCase(hashSuffix)) {
                logger.warn("Contraseña comprometida detectada en Leak-Lookup");
                return true;
            }
        }
        
        logger.debug("Contraseña no encontrada en brechas de Leak-Lookup");
        return false;
    }
    
    /**
     * Calcula el hash SHA-1 de una contraseña.
     * SHA-1 es utilizado por Leak-Lookup y Have I Been Pwned para compatibilidad.
     */
    private String computeSHA1(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedHash);
    }
    
    /**
     * Convierte bytes a representación hexadecimal.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Mensaje informativo para el usuario cuando la contraseña es comprometida
     */
    public static String getCompromisedPasswordMessage() {
        return "La contraseña seleccionada ha sido comprometida en brechas de datos públicas. " +
               "Por tu seguridad, por favor selecciona una contraseña diferente.";
    }
}


