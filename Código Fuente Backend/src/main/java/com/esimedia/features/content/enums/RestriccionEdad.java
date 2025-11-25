package com.esimedia.features.content.enums;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RestriccionEdad {
    ADULTOS(18),
    ADOLESCENTES(16),
    ESCOLARES(12),
    INFANTIL(7),
    PREESCOLAR(3);
    
    private final int valor;
    
    private static final Set<Integer> VALID_VALUES = Arrays.stream(values())
        .map(RestriccionEdad::getValor)
        .collect(Collectors.toSet());
    
    RestriccionEdad(int valor) {
        this.valor = valor;
    }
    
    public int getValor() {
        return valor;
    }
    
    public static boolean isValidValue(Integer value) {
        return VALID_VALUES.contains(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}