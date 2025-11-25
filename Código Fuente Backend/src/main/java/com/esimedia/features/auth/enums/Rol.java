package com.esimedia.features.auth.enums;

public enum Rol {
    NORMAL("NORMAL"),
    CREADOR("CREADOR"),
    ADMINISTRADOR("ADMINISTRADOR");
    
    private final String valor;
    
    Rol(String valor) {
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