package com.esimedia.features.auth.enums;

/**
 * Tipos de verificación disponibles
 */

import lombok.Getter;

@Getter
public enum TipoVerificacion {
    EMAIL_2FA("Email de segundo factor"),
    TOTP_SETUP("Configuración TOTP"),
    LOGIN_EMAIL("Código de login por email");

    private final String descripcion;

    TipoVerificacion(String descripcion) {
        this.descripcion = descripcion;
    }
}
