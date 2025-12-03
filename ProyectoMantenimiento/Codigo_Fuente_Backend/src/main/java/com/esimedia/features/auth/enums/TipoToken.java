package com.esimedia.features.auth.enums;

public enum TipoToken {
    CONFIRMACION_CUENTA("CONFIRMACION_CUENTA"),
    RESET_PASSWORD("RESET_PASSWORD"),
    REGISTRO("REGISTRO"),
    RECUPERACION_PASSWORD("RECUPERACION_PASSWORD"),
    CONFIRMACION_EMAIL("CONFIRMACION_EMAIL"),
    ACCESO("ACCESO"),
    REFRESH("REFRESH");

    private final String valor;

    TipoToken(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return valor;
    }
}