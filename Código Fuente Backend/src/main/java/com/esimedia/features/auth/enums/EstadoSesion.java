package com.esimedia.features.auth.enums;

public enum EstadoSesion {
    ACTIVA("ACTIVA"),
    EXPIRADA("EXPIRADA"),
    BLOQUEADA("BLOQUEADA");
    
    private final String valor;
    
    EstadoSesion(String valor) {
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