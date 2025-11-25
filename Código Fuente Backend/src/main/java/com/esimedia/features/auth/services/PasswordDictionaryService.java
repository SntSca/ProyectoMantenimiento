package com.esimedia.features.auth.services;

import com.esimedia.features.auth.repository.CommonPasswordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Servicio para verificar contraseñas contra un diccionario de contraseñas típicas.
 * Lee hashes SHA-256 desde la base de datos MongoDB para evitar cálculos en runtime.
 * 
 * Nota: El usuario nunca ve el diccionario, solo se comprueba internamente en el registro.
 */
@Service
public class PasswordDictionaryService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordDictionaryService.class);
    
    private final CommonPasswordRepository commonPasswordRepository;
    
    private static final String HASH_ALGORITHM = "SHA-256";
    
    @Autowired
    public PasswordDictionaryService(CommonPasswordRepository commonPasswordRepository) {
        this.commonPasswordRepository = commonPasswordRepository;
    }
    
    /**
     * Hashea una contraseña con SHA-256 para comparar con los hashes del diccionario
     */
    public String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedHash);
    }
    
    /**
     * Convierte bytes a representación hexadecimal
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
     * Verifica si una contraseña está en el diccionario de contraseñas típicas.
     * 
     * @param password La contraseña a verificar
     * @return true si la contraseña está en el diccionario (es débil), false en caso contrario
     */
    public boolean isPasswordInDictionary(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        try {
            String hashedPassword = hashPassword(password);
            boolean isFound = commonPasswordRepository.existsByHash(hashedPassword);
            
            if (isFound) {
                logger.warn("Intento de registro con contraseña del diccionario detectado");
            }
            
            return isFound;
        } 
        catch (NoSuchAlgorithmException e) {
            logger.error("Error al hashear contraseña durante verificación: {}", e.getMessage(), e);
            // En caso de error, permitir el registro para no bloquear al usuario
            return false;
        }
    }
    
    /**
     * Obtiene el tamaño actual del diccionario cargado
     */
    public long getDictionarySize() {
        return commonPasswordRepository.count();
    }
    
    /**
     * Verifica si el diccionario está cargado
     */
    public boolean isDictionaryLoaded() {
        return commonPasswordRepository.count() > 0;
    }
}
