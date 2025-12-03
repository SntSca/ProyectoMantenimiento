package com.esimedia.features.content.enums;

public enum Resolucion {
    UHD_2160("2160"),
    FHD_1080("1080"), 
    HD_720("720");
    
    private final String valor;
    
    Resolucion(String valor) {
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