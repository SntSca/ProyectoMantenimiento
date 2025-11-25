package com.esimedia.shared.security;

/**
 * Excepción personalizada para errores relacionados con el servicio de antivirus ClamAV.
 */
public class AntivirusException extends RuntimeException {

    public AntivirusException(String message) {
        super(message);
    }

    public AntivirusException(String message, Throwable cause) {
        super(message, cause);
    }
}