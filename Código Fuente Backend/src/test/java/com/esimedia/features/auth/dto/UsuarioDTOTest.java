package com.esimedia.features.auth.dto;

import com.esimedia.features.auth.enums.Rol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDTOTest {

    private static final String TEST_NOMBRE = "Juan";
    private static final String TEST_APELLIDOS = "Pérez García";
    private static final String TEST_EMAIL = "juan.perez@example.com";
    private static final String TEST_ALIAS = "jperez";
    private static final String TEST_PASSWORD = "password123";
    private static final Rol TEST_ROL = Rol.NORMAL;
    private static final String TEST_FOTO_PERFIL = "base64encodedimage";
    private static final String TEST_FORMATO_FOTO = "image/jpeg";

    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        usuarioDTO = new UsuarioDTO();
    }

    // ========== TESTS DE GETTERS Y SETTERS ==========
    
    @Test
    void testGettersSettersBasicFields() {
        // When
        usuarioDTO.setNombre(TEST_NOMBRE);
        usuarioDTO.setApellidos(TEST_APELLIDOS);
        usuarioDTO.setEmail(TEST_EMAIL);
        usuarioDTO.setAlias(TEST_ALIAS);
        usuarioDTO.setPassword(TEST_PASSWORD);
        usuarioDTO.setRol(TEST_ROL);

        // Then
        assertEquals(TEST_NOMBRE, usuarioDTO.getNombre());
        assertEquals(TEST_APELLIDOS, usuarioDTO.getApellidos());
        assertEquals(TEST_EMAIL, usuarioDTO.getEmail());
        assertEquals(TEST_ALIAS, usuarioDTO.getAlias());
        assertEquals(TEST_PASSWORD, usuarioDTO.getPassword());
        assertEquals(TEST_ROL, usuarioDTO.getRol());
    }

    @Test
    void testGettersSettersFotoPerfilFields() {
        // When
        usuarioDTO.setFotoPerfil(TEST_FOTO_PERFIL);
        usuarioDTO.setFormatoFotoPerfil(TEST_FORMATO_FOTO);

        // Then
        assertEquals(TEST_FOTO_PERFIL, usuarioDTO.getFotoPerfil());
        assertEquals(TEST_FORMATO_FOTO, usuarioDTO.getFormatoFotoPerfil());
    }

    @Test
    void testNullValues() {
        // When & Then
        assertNull(usuarioDTO.getNombre());
        assertNull(usuarioDTO.getApellidos());
        assertNull(usuarioDTO.getEmail());
        assertNull(usuarioDTO.getAlias());
        assertNull(usuarioDTO.getPassword());
        assertNull(usuarioDTO.getRol());
        assertNull(usuarioDTO.getFotoPerfil());
        assertNull(usuarioDTO.getFormatoFotoPerfil());
    }

    @Test
    void testSetNullValues() {
        // Given - primero establecer valores
        usuarioDTO.setNombre(TEST_NOMBRE);
        usuarioDTO.setFotoPerfil(TEST_FOTO_PERFIL);

        // When - establecer a null
        usuarioDTO.setNombre(null);
        usuarioDTO.setApellidos(null);
        usuarioDTO.setEmail(null);
        usuarioDTO.setAlias(null);
        usuarioDTO.setPassword(null);
        usuarioDTO.setRol(null);
        usuarioDTO.setFotoPerfil(null);
        usuarioDTO.setFormatoFotoPerfil(null);

        // Then
        assertNull(usuarioDTO.getNombre());
        assertNull(usuarioDTO.getApellidos());
        assertNull(usuarioDTO.getEmail());
        assertNull(usuarioDTO.getAlias());
        assertNull(usuarioDTO.getPassword());
        assertNull(usuarioDTO.getRol());
        assertNull(usuarioDTO.getFotoPerfil());
        assertNull(usuarioDTO.getFormatoFotoPerfil());
    }

    // ========== TESTS DE EQUALS ==========

    @Test
    void testEquals_sameObject() {
        // When & Then
        assertEquals(usuarioDTO, usuarioDTO);
    }

    @Test
    void testEquals_equalObjects() {
        // Given
        UsuarioDTO usuario1 = createFullUsuarioDTO();
        UsuarioDTO usuario2 = createFullUsuarioDTO();

        // When & Then
        assertEquals(usuario1, usuario2);
        assertEquals(usuario2, usuario1);
    }

    @Test
    void testEquals_differentEmail() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setEmail(TEST_EMAIL);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setEmail("diferente@example.com");

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_differentNombre() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setNombre(TEST_NOMBRE);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setNombre("Pedro");

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_differentApellidos() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setApellidos(TEST_APELLIDOS);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setApellidos("López Martínez");

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_differentAlias() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setAlias(TEST_ALIAS);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setAlias("otrousuario");

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_differentPassword() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setPassword(TEST_PASSWORD);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setPassword("differentPassword");

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_differentRol() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setRol(Rol.NORMAL);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setRol(Rol.ADMINISTRADOR);

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_differentFotoPerfil() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setFotoPerfil(TEST_FOTO_PERFIL);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setFotoPerfil("differentBase64Image");

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_differentFormatoFoto() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setFormatoFotoPerfil(TEST_FORMATO_FOTO);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setFormatoFotoPerfil("image/png");

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_nullObject() {
        // When & Then
        assertNotEquals(null, usuarioDTO);
    }

    @Test
    void testEquals_differentClass() {
        // Given
        String differentObject = "not a UsuarioDTO";

        // When & Then
        assertNotEquals(usuarioDTO, differentObject);
    }

    @Test
    void testEquals_oneFieldNull() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setEmail(TEST_EMAIL);
        usuario1.setNombre(null);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setEmail(TEST_EMAIL);
        usuario2.setNombre(TEST_NOMBRE);

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEquals_bothFieldsNull() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        UsuarioDTO usuario2 = new UsuarioDTO();

        // When & Then
        assertEquals(usuario1, usuario2);
    }

    // ========== TESTS DE HASHCODE ==========

    @Test
    void testHashCode_sameObject() {
        // When
        int hash1 = usuarioDTO.hashCode();
        int hash2 = usuarioDTO.hashCode();

        // Then
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCode_equalObjects() {
        // Given
        UsuarioDTO usuario1 = createFullUsuarioDTO();
        UsuarioDTO usuario2 = createFullUsuarioDTO();

        // When & Then
        assertEquals(usuario1.hashCode(), usuario2.hashCode());
    }

    @Test
    void testHashCode_differentObjects() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setEmail(TEST_EMAIL);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setEmail("diferente@example.com");

        // When & Then
        assertNotEquals(usuario1.hashCode(), usuario2.hashCode());
    }

    @Test
    void testHashCode_nullFields() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        UsuarioDTO usuario2 = new UsuarioDTO();

        // When & Then
        assertEquals(usuario1.hashCode(), usuario2.hashCode());
    }

    @Test
    void testHashCode_someNullFields() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setEmail(TEST_EMAIL);
        usuario1.setNombre(null);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setEmail(TEST_EMAIL);
        usuario2.setNombre(null);

        // When & Then
        assertEquals(usuario1.hashCode(), usuario2.hashCode());
    }

    @Test
    void testHashCode_differentRoles() {
        // Given
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setRol(Rol.NORMAL);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setRol(Rol.ADMINISTRADOR);

        // When & Then
        assertNotEquals(usuario1.hashCode(), usuario2.hashCode());
    }

    @Test
    void testHashCode_allFieldsPopulated() {
        // Given
        UsuarioDTO usuario = createFullUsuarioDTO();

        // When
        int hashCode = usuario.hashCode();

        // Then
        assertNotEquals(0, hashCode);
    }

    // ========== TESTS DE TOSTRING ==========

    @Test
    void testToString_withAllFields() {
        // Given
        UsuarioDTO usuario = createFullUsuarioDTO();

        // When
        String resultado = usuario.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("UsuarioDTO"));
        assertTrue(resultado.contains(TEST_EMAIL));
        assertTrue(resultado.contains(TEST_NOMBRE));
        assertTrue(resultado.contains(TEST_ALIAS));
    }

    @Test
    void testToString_withNullFields() {
        // When
        String resultado = usuarioDTO.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("UsuarioDTO"));
    }

    @Test
    void testToString_withFotoFields() {
        // Given
        usuarioDTO.setFotoPerfil(TEST_FOTO_PERFIL);
        usuarioDTO.setFormatoFotoPerfil(TEST_FORMATO_FOTO);

        // When
        String resultado = usuarioDTO.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("fotoPerfil"));
        assertTrue(resultado.contains("formatoFotoPerfil"));
    }

    // ========== TESTS DE CONSISTENCIA EQUALS-HASHCODE ==========

    @Test
    void testEqualsHashCodeConsistency() {
        // Given
        UsuarioDTO usuario1 = createFullUsuarioDTO();
        UsuarioDTO usuario2 = createFullUsuarioDTO();

        // When & Then - Si equals es true, hashCode debe ser igual
        if (usuario1.equals(usuario2)) {
            assertEquals(usuario1.hashCode(), usuario2.hashCode(),
                "Si dos objetos son iguales según equals(), deben tener el mismo hashCode()");
        }
    }

    @Test
    void testEqualsTransitivity() {
        // Given
        UsuarioDTO usuario1 = createFullUsuarioDTO();
        UsuarioDTO usuario2 = createFullUsuarioDTO();
        UsuarioDTO usuario3 = createFullUsuarioDTO();

        // When & Then - Transitividad: si a=b y b=c, entonces a=c
        assertEquals(usuario1, usuario2);
        assertEquals(usuario2, usuario3);
        assertEquals(usuario1, usuario3);
    }

    // ========== MÉTODO AUXILIAR ==========

    private UsuarioDTO createFullUsuarioDTO() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre(TEST_NOMBRE);
        dto.setApellidos(TEST_APELLIDOS);
        dto.setEmail(TEST_EMAIL);
        dto.setAlias(TEST_ALIAS);
        dto.setPassword(TEST_PASSWORD);
        dto.setRol(TEST_ROL);
        dto.setFotoPerfil(TEST_FOTO_PERFIL);
        dto.setFormatoFotoPerfil(TEST_FORMATO_FOTO);
        return dto;
    }
}