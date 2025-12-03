package com.esimedia.features.auth.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.esimedia.features.user_management.dto.AdminProfileUpdateDTO;

import static org.junit.jupiter.api.Assertions.*;

class AdminProfileUpdateDTOTest {

    private static final String TEST_NOMBRE = "Juan";
    private static final String TEST_APELLIDOS = "Pérez García";
    private static final String TEST_ALIAS = "jperez";
    private static final String TEST_FOTO_PERFIL = "base64encodedimage";

    private AdminProfileUpdateDTO dto;

    @BeforeEach
    void setUp() {
        dto = new AdminProfileUpdateDTO();
    }

    // ========== TESTS DEL CONSTRUCTOR ==========

    @Test
    void testConstructor() {
        // When
        AdminProfileUpdateDTO newDto = new AdminProfileUpdateDTO();

        // Then
        assertNotNull(newDto);
        assertNull(newDto.getNombre());
        assertNull(newDto.getApellidos());
        assertNull(newDto.getAlias());
        assertNull(newDto.getFotoPerfil());
    }

    // ========== TESTS DE GETTERS Y SETTERS ==========

    @Test
    void testGetSetNombre() {
        // When
        dto.setNombre(TEST_NOMBRE);

        // Then
        assertEquals(TEST_NOMBRE, dto.getNombre());
    }

    @Test
    void testGetSetApellidos() {
        // When
        dto.setApellidos(TEST_APELLIDOS);

        // Then
        assertEquals(TEST_APELLIDOS, dto.getApellidos());
    }

    @Test
    void testGetSetAlias() {
        // When
        dto.setAlias(TEST_ALIAS);

        // Then
        assertEquals(TEST_ALIAS, dto.getAlias());
    }

    @Test
    void testGetSetFotoPerfil() {
        // When
        dto.setFotoPerfil(TEST_FOTO_PERFIL);

        // Then
        assertEquals(TEST_FOTO_PERFIL, dto.getFotoPerfil());
    }

    @Test
    void testGettersReturnNullByDefault() {
        // When & Then
        assertNull(dto.getNombre());
        assertNull(dto.getApellidos());
        assertNull(dto.getAlias());
        assertNull(dto.getFotoPerfil());
    }

    @Test
    void testSetNullValues() {
        // Given
        dto.setNombre(TEST_NOMBRE);
        dto.setApellidos(TEST_APELLIDOS);
        dto.setAlias(TEST_ALIAS);
        dto.setFotoPerfil(TEST_FOTO_PERFIL);

        // When
        dto.setNombre(null);
        dto.setApellidos(null);
        dto.setAlias(null);
        dto.setFotoPerfil(null);

        // Then
        assertNull(dto.getNombre());
        assertNull(dto.getApellidos());
        assertNull(dto.getAlias());
        assertNull(dto.getFotoPerfil());
    }

    @Test
    void testSetAllFields() {
        // When
        dto.setNombre(TEST_NOMBRE);
        dto.setApellidos(TEST_APELLIDOS);
        dto.setAlias(TEST_ALIAS);
        dto.setFotoPerfil(TEST_FOTO_PERFIL);

        // Then
        assertEquals(TEST_NOMBRE, dto.getNombre());
        assertEquals(TEST_APELLIDOS, dto.getApellidos());
        assertEquals(TEST_ALIAS, dto.getAlias());
        assertEquals(TEST_FOTO_PERFIL, dto.getFotoPerfil());
    }

    // ========== TESTS DE EQUALS ==========

    @Test
    void testEquals_sameObject() {
        // When & Then
        assertEquals(dto, dto);
    }

    @Test
    void testEquals_equalObjects() {
        // Given
        AdminProfileUpdateDTO dto1 = createFullDTO();
        AdminProfileUpdateDTO dto2 = createFullDTO();

        // When & Then
        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }

    @Test
    void testEquals_differentNombre() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setNombre(TEST_NOMBRE);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setNombre("Pedro");

        // When & Then
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_differentApellidos() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setApellidos(TEST_APELLIDOS);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setApellidos("López Martínez");

        // When & Then
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_differentAlias() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setAlias(TEST_ALIAS);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setAlias("otrousuario");

        // When & Then
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_differentFotoPerfil() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setFotoPerfil(TEST_FOTO_PERFIL);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setFotoPerfil("differentBase64Image");

        // When & Then
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_nullObject() {
        // When & Then
        assertNotEquals(null, dto);
    }

    @Test
    void testEquals_differentClass() {
        // Given
        String differentObject = "not an AdminProfileUpdateDTO";

        // When & Then
        assertNotEquals(dto, differentObject);
    }

    @Test
    void testEquals_oneFieldNull() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setNombre(TEST_NOMBRE);
        dto1.setApellidos(null);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setNombre(TEST_NOMBRE);
        dto2.setApellidos(TEST_APELLIDOS);

        // When & Then
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_bothFieldsNull() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();

        // When & Then
        assertEquals(dto1, dto2);
    }

    @Test
    void testEquals_allFieldsNull() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setNombre(null);
        dto1.setApellidos(null);
        dto1.setAlias(null);
        dto1.setFotoPerfil(null);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setNombre(null);
        dto2.setApellidos(null);
        dto2.setAlias(null);
        dto2.setFotoPerfil(null);

        // When & Then
        assertEquals(dto1, dto2);
    }

    @Test
    void testEquals_mixedNullFields() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setNombre(TEST_NOMBRE);
        dto1.setApellidos(null);
        dto1.setAlias(TEST_ALIAS);
        dto1.setFotoPerfil(null);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setNombre(TEST_NOMBRE);
        dto2.setApellidos(null);
        dto2.setAlias(TEST_ALIAS);
        dto2.setFotoPerfil(null);

        // When & Then
        assertEquals(dto1, dto2);
    }

    @Test
    void testEquals_nullVsNonNull() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setNombre(null);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setNombre(TEST_NOMBRE);

        // When & Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto2, dto1);
    }

    // ========== TESTS DE HASHCODE ==========

    @Test
    void testHashCode_sameObject() {
        // When
        int hash1 = dto.hashCode();
        int hash2 = dto.hashCode();

        // Then
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCode_equalObjects() {
        // Given
        AdminProfileUpdateDTO dto1 = createFullDTO();
        AdminProfileUpdateDTO dto2 = createFullDTO();

        // When & Then
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testHashCode_differentObjects() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setNombre(TEST_NOMBRE);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setNombre("Pedro");

        // When & Then
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testHashCode_nullFields() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();

        // When & Then
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testHashCode_someNullFields() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setNombre(TEST_NOMBRE);
        dto1.setApellidos(null);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setNombre(TEST_NOMBRE);
        dto2.setApellidos(null);

        // When & Then
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testHashCode_allFieldsPopulated() {
        // Given
        AdminProfileUpdateDTO localDto = createFullDTO();

        // When
        int hashCode = localDto.hashCode();

        // Then
        assertNotEquals(0, hashCode);
    }

    @Test
    void testHashCode_differentAlias() {
        // Given
        AdminProfileUpdateDTO dto1 = new AdminProfileUpdateDTO();
        dto1.setAlias(TEST_ALIAS);

        AdminProfileUpdateDTO dto2 = new AdminProfileUpdateDTO();
        dto2.setAlias("otrousuario");

        // When & Then
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    // ========== TESTS DE TOSTRING ==========

    @Test
    void testToString_withAllFields() {
        // Given
        AdminProfileUpdateDTO localDto = createFullDTO();

        // When
        String resultado = localDto.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("AdminProfileUpdateDTO"));
        assertTrue(resultado.contains(TEST_NOMBRE));
        assertTrue(resultado.contains(TEST_APELLIDOS));
        assertTrue(resultado.contains(TEST_ALIAS));
        assertTrue(resultado.contains(TEST_FOTO_PERFIL));
    }

    @Test
    void testToString_withNullFields() {
        // When
        String resultado = dto.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("AdminProfileUpdateDTO"));
    }

    @Test
    void testToString_withPartialFields() {
        // Given
        dto.setNombre(TEST_NOMBRE);
        dto.setAlias(TEST_ALIAS);

        // When
        String resultado = dto.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("AdminProfileUpdateDTO"));
        assertTrue(resultado.contains(TEST_NOMBRE));
        assertTrue(resultado.contains(TEST_ALIAS));
    }

    @Test
    void testToString_containsFieldNames() {
        // Given
        AdminProfileUpdateDTO localDto = createFullDTO();

        // When
        String resultado = localDto.toString();

        // Then
        assertTrue(resultado.contains("nombre"));
        assertTrue(resultado.contains("apellidos"));
        assertTrue(resultado.contains("alias"));
        assertTrue(resultado.contains("fotoPerfil"));
    }

    // ========== TESTS DE CONSISTENCIA EQUALS-HASHCODE ==========

    @Test
    void testEqualsHashCodeConsistency() {
        // Given
        AdminProfileUpdateDTO dto1 = createFullDTO();
        AdminProfileUpdateDTO dto2 = createFullDTO();

        // When & Then
        if (dto1.equals(dto2)) {
            assertEquals(dto1.hashCode(), dto2.hashCode(),
                "Si dos objetos son iguales según equals(), deben tener el mismo hashCode()");
        }
    }

    @Test
    void testEqualsSymmetry() {
        // Given
        AdminProfileUpdateDTO dto1 = createFullDTO();
        AdminProfileUpdateDTO dto2 = createFullDTO();

        // When & Then
        assertEquals(dto1.equals(dto2), dto2.equals(dto1),
            "equals() debe ser simétrico: a.equals(b) == b.equals(a)");
    }

    @Test
    void testEqualsTransitivity() {
        // Given
        AdminProfileUpdateDTO dto1 = createFullDTO();
        AdminProfileUpdateDTO dto2 = createFullDTO();
        AdminProfileUpdateDTO dto3 = createFullDTO();

        // When & Then
        assertEquals(dto1, dto2);
        assertEquals(dto2, dto3);
        assertEquals(dto1, dto3, "equals() debe ser transitivo");
    }

    @Test
    void testEqualsReflexivity() {
        // Given
        AdminProfileUpdateDTO dto1 = createFullDTO();

        // When & Then
        assertEquals(dto1, dto1, "equals() debe ser reflexivo");
    }

    @Test
    void testHashCodeConsistency() {
        // Given
        AdminProfileUpdateDTO dto1 = createFullDTO();

        // When
        int hash1 = dto1.hashCode();
        int hash2 = dto1.hashCode();

        // Then
        assertEquals(hash1, hash2,
            "Múltiples invocaciones de hashCode() deben devolver el mismo valor");
    }

    // ========== TESTS DE CASOS EDGE ==========

    @Test
    void testEmptyStrings() {
        // When
        dto.setNombre("");
        dto.setApellidos("");
        dto.setAlias("");
        dto.setFotoPerfil("");

        // Then
        assertEquals("", dto.getNombre());
        assertEquals("", dto.getApellidos());
        assertEquals("", dto.getAlias());
        assertEquals("", dto.getFotoPerfil());
    }

    @Test
    void testWhitespaceStrings() {
        // When
        dto.setNombre("   ");
        dto.setApellidos("   ");
        dto.setAlias("   ");

        // Then
        assertEquals("   ", dto.getNombre());
        assertEquals("   ", dto.getApellidos());
        assertEquals("   ", dto.getAlias());
    }

    @Test
    void testLongStrings() {
        // Given
        String longString = "a".repeat(1000);

        // When
        dto.setNombre(longString);
        dto.setFotoPerfil(longString);

        // Then
        assertEquals(longString, dto.getNombre());
        assertEquals(longString, dto.getFotoPerfil());
    }

    @Test
    void testSpecialCharacters() {
        // Given
        String specialChars = "ñáéíóú@#$%&*()";

        // When
        dto.setNombre(specialChars);
        dto.setAlias(specialChars);

        // Then
        assertEquals(specialChars, dto.getNombre());
        assertEquals(specialChars, dto.getAlias());
    }

    // ========== MÉTODO AUXILIAR ==========

    private AdminProfileUpdateDTO createFullDTO() {
        AdminProfileUpdateDTO localDto = new AdminProfileUpdateDTO();
        localDto.setNombre(TEST_NOMBRE);
        localDto.setApellidos(TEST_APELLIDOS);
        localDto.setAlias(TEST_ALIAS);
        localDto.setFotoPerfil(TEST_FOTO_PERFIL);
        return localDto;
    }
}