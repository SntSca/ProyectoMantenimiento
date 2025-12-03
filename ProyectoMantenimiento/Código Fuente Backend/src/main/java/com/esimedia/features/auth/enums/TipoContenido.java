package com.esimedia.features.auth.enums;

public enum TipoContenido {
    VIDEO("VIDEO"),
    AUDIO("AUDIO");
    
    private final String valor;
    
    TipoContenido(String valor) {
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