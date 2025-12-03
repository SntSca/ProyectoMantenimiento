package com.esimedia.features.auth.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

/**
 * Servicio centralizado para la gestión de contraseñas.
 * Maneja el encoding y verificación de contraseñas usando Argon2id como algoritmo principal,
 * con soporte para algoritmos legacy (BCrypt y SHA-256).
 */
@Service
public class PasswordService {

    @Value("${app.security.pepper}")
    private String pepper;

    /**
     * Codifica una contraseña usando Argon2id con pepper
     * 
     * @param password la contraseña en texto plano
     * @return la contraseña codificada
     */
    public String encodePassword(String password) {
        String passwordWithPepper = password + pepper;
        // Usar Argon2id
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        int memory = 65536;
        int iterations = 3;
        int parallelism = 1;
        String hash = argon2.hash(iterations, memory, parallelism, passwordWithPepper.toCharArray());
        argon2.wipeArray(passwordWithPepper.toCharArray());
        return hash;
    }

    /**
     * Verifica si una contraseña coincide con el hash almacenado
     * Soporta múltiples algoritmos: Argon2id, BCrypt y SHA-256 (legacy)
     * 
     * @param rawPassword la contraseña en texto plano
     * @param storedHash el hash almacenado
     * @return true si las contraseñas coinciden
     */
    public boolean matchesPassword(String rawPassword, String storedHash) {
        String passwordWithPepper = rawPassword + pepper;
        boolean result = false;
        
        if (storedHash.startsWith("$argon2id$")) {
            Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
            result = argon2.verify(storedHash, passwordWithPepper.toCharArray());
            argon2.wipeArray(passwordWithPepper.toCharArray());
        } 
        else if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            result = BCrypt.checkpw(passwordWithPepper, storedHash);
        } 
        else {
            // Fallback SHA-256 (legacy, no recomendado)
            result = MessageDigest.isEqual(encodePassword(rawPassword).getBytes(), storedHash.getBytes());
        }
        return result;
    }
}