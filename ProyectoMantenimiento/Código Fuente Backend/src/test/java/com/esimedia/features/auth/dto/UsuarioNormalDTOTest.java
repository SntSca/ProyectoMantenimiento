package com.esimedia.features.auth.dto;

import com.esimedia.features.auth.enums.Rol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioNormalDTOTest {

    private static final String TEST_NOMBRE = "María";
    private static final String TEST_APELLIDOS = "González López";
    private static final String TEST_EMAIL = "maria.gonzalez@example.com";
    private static final String TEST_ALIAS = "mgonzalez";
    private static final String TEST_PASSWORD = "password456";
    private static final Rol TEST_ROL = Rol.NORMAL;
    private static final Date TEST_FECHA_NACIMIENTO = new Date();
    private static final Boolean TEST_FLAG_VIP = true;

    @Mock
    private UsuarioNormalDTO mockUsuarioNormalDTO;

    @Test
    void testGettersSettersWithMock() {
        // Given
        when(mockUsuarioNormalDTO.getNombre()).thenReturn(TEST_NOMBRE);
        when(mockUsuarioNormalDTO.getApellidos()).thenReturn(TEST_APELLIDOS);
        when(mockUsuarioNormalDTO.getEmail()).thenReturn(TEST_EMAIL);
        when(mockUsuarioNormalDTO.getAlias()).thenReturn(TEST_ALIAS);
        when(mockUsuarioNormalDTO.getPassword()).thenReturn(TEST_PASSWORD);
        when(mockUsuarioNormalDTO.getRol()).thenReturn(TEST_ROL);
        when(mockUsuarioNormalDTO.getFechaNacimiento()).thenReturn(TEST_FECHA_NACIMIENTO);
        when(mockUsuarioNormalDTO.getFlagVIP()).thenReturn(TEST_FLAG_VIP);

        // When & Then
        assertEquals(TEST_NOMBRE, mockUsuarioNormalDTO.getNombre());
        assertEquals(TEST_APELLIDOS, mockUsuarioNormalDTO.getApellidos());
        assertEquals(TEST_EMAIL, mockUsuarioNormalDTO.getEmail());
        assertEquals(TEST_ALIAS, mockUsuarioNormalDTO.getAlias());
        assertEquals(TEST_PASSWORD, mockUsuarioNormalDTO.getPassword());
        assertEquals(TEST_ROL, mockUsuarioNormalDTO.getRol());
        assertEquals(TEST_FECHA_NACIMIENTO, mockUsuarioNormalDTO.getFechaNacimiento());
        assertEquals(TEST_FLAG_VIP, mockUsuarioNormalDTO.getFlagVIP());
    }

    @Test
    void testGettersSettersWithRealObject() {
        // Given
        UsuarioNormalDTO usuarioNormalDTO = new UsuarioNormalDTO();

        // When
        usuarioNormalDTO.setNombre(TEST_NOMBRE);
        usuarioNormalDTO.setApellidos(TEST_APELLIDOS);
        usuarioNormalDTO.setEmail(TEST_EMAIL);
        usuarioNormalDTO.setAlias(TEST_ALIAS);
        usuarioNormalDTO.setPassword(TEST_PASSWORD);
        usuarioNormalDTO.setRol(TEST_ROL);
        usuarioNormalDTO.setFechaNacimiento(TEST_FECHA_NACIMIENTO);
        usuarioNormalDTO.setFlagVIP(TEST_FLAG_VIP);

        // Then
        assertEquals(TEST_NOMBRE, usuarioNormalDTO.getNombre());
        assertEquals(TEST_APELLIDOS, usuarioNormalDTO.getApellidos());
        assertEquals(TEST_EMAIL, usuarioNormalDTO.getEmail());
        assertEquals(TEST_ALIAS, usuarioNormalDTO.getAlias());
        assertEquals(TEST_PASSWORD, usuarioNormalDTO.getPassword());
        assertEquals(TEST_ROL, usuarioNormalDTO.getRol());
        assertEquals(TEST_FECHA_NACIMIENTO, usuarioNormalDTO.getFechaNacimiento());
        assertEquals(TEST_FLAG_VIP, usuarioNormalDTO.getFlagVIP());
    }

    @Test
    void testInheritanceFromUsuarioDTO() {
        // Given
        UsuarioNormalDTO usuarioNormalDTO = new UsuarioNormalDTO();

        // When
        usuarioNormalDTO.setEmail(TEST_EMAIL);
        usuarioNormalDTO.setFechaNacimiento(TEST_FECHA_NACIMIENTO);

        // Then
        assertTrue(usuarioNormalDTO instanceof UsuarioDTO);
        assertEquals(TEST_EMAIL, usuarioNormalDTO.getEmail());
        assertEquals(TEST_FECHA_NACIMIENTO, usuarioNormalDTO.getFechaNacimiento());
    }

    @Test
    void testNotEquals() {
        // Given
        UsuarioNormalDTO usuario1 = new UsuarioNormalDTO();
        usuario1.setFechaNacimiento(TEST_FECHA_NACIMIENTO);

        UsuarioNormalDTO usuario2 = new UsuarioNormalDTO();
        usuario2.setFechaNacimiento(new Date(0));

        // When & Then
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testToString() {
        // Given
        UsuarioNormalDTO usuarioNormalDTO = new UsuarioNormalDTO();
        usuarioNormalDTO.setNombre(TEST_NOMBRE);
        usuarioNormalDTO.setApellidos(TEST_APELLIDOS);
        usuarioNormalDTO.setEmail(TEST_EMAIL);
        usuarioNormalDTO.setFechaNacimiento(TEST_FECHA_NACIMIENTO);

        // When
        String resultado = usuarioNormalDTO.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("UsuarioNormalDTO"));
    }

    @Test
    void testNullFechaNacimiento() {
        // Given
        UsuarioNormalDTO usuarioNormalDTO = new UsuarioNormalDTO();

        // When & Then
        assertNull(usuarioNormalDTO.getFechaNacimiento());
    }

    @Test
    void testSetNullFechaNacimiento() {
        // Given
        UsuarioNormalDTO usuarioNormalDTO = new UsuarioNormalDTO();

        // When
        usuarioNormalDTO.setFechaNacimiento(null);

        // Then
        assertNull(usuarioNormalDTO.getFechaNacimiento());
    }

    @Test
    void testSpecificFechaNacimientoValue() {
        // Given
        UsuarioNormalDTO usuarioNormalDTO = new UsuarioNormalDTO();
        Date fechaEspecifica = new Date(946684800000L);

        // When
        usuarioNormalDTO.setFechaNacimiento(fechaEspecifica);

        // Then
        assertEquals(fechaEspecifica, usuarioNormalDTO.getFechaNacimiento());
        assertEquals(946684800000L, usuarioNormalDTO.getFechaNacimiento().getTime());
    }
}