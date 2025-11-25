package com.esimedia.features.favoritos.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import jakarta.validation.ConstraintViolation;

class FavoritoDTOTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testGetterSetter() {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdContenido("abc123");
        assertEquals("abc123", dto.getIdContenido());
    }

    @Test
    void testEqualsSameObject() {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdContenido("x");
        assertEquals(dto, dto); // reflexividad
    }

    @Test
    void testEqualsDifferentObjectSameValue() {
        FavoritoDTO d1 = new FavoritoDTO();
        d1.setIdContenido("x");

        FavoritoDTO d2 = new FavoritoDTO();
        d2.setIdContenido("x");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void testEqualsDifferentValues() {
        FavoritoDTO d1 = new FavoritoDTO();
        d1.setIdContenido("x");

        FavoritoDTO d2 = new FavoritoDTO();
        d2.setIdContenido("y");

        assertNotEquals(d1, d2);
    }

    @Test
    void testEqualsNull() {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdContenido("x");

        assertNotEquals(dto, null);
    }

    @Test
    void testEqualsDifferentType() {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdContenido("x");

        assertNotEquals(dto, "un string");
    }

    @Test
    void testHashCodeDifferentValues() {
        FavoritoDTO d1 = new FavoritoDTO();
        d1.setIdContenido("x");

        FavoritoDTO d2 = new FavoritoDTO();
        d2.setIdContenido("y");

        assertNotEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void testToStringNotNull() {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdContenido("x");

        assertNotNull(dto.toString());
        assertTrue(dto.toString().contains("idContenido=x"));
    }

    @Test
    void testValidacionIdContenidoVacio() {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdContenido("");

        Set<ConstraintViolation<FavoritoDTO>> violaciones = validator.validate(dto);

        assertFalse(violaciones.isEmpty());
    }

    @Test
    void testValidacionIdContenidoCorrecto() {
        FavoritoDTO dto = new FavoritoDTO();
        dto.setIdContenido("valid");

        Set<ConstraintViolation<FavoritoDTO>> violaciones = validator.validate(dto);

        assertTrue(violaciones.isEmpty());
    }
}