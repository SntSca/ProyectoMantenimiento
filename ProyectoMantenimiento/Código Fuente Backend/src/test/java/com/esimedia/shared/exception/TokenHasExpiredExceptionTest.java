package com.esimedia.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class TokenHasExpiredExceptionTest {

    @Test
    @DisplayName("Debe crear excepción con constructor por defecto")
    void testConstructorPorDefecto() {
        // When
        TokenHasExpiredException exception = new TokenHasExpiredException();

        // Then
        assertNotNull(exception);
        assertEquals("El token ha expirado", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Debe crear excepción con mensaje personalizado")
    void testConstructorConMensaje() {
        // Given
        String mensaje = "Token JWT expirado después de 24 horas";

        // When
        TokenHasExpiredException exception = new TokenHasExpiredException(mensaje);

        // Then
        assertNotNull(exception);
        assertEquals(mensaje, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Debe crear excepción con mensaje y causa")
    void testConstructorConMensajeYCausa() {
        // Given
        String mensaje = "El token de acceso ha caducado";
        Throwable causa = new IllegalStateException("Estado inválido del token");

        // When
        TokenHasExpiredException exception = new TokenHasExpiredException(mensaje, causa);

        // Then
        assertNotNull(exception);
        assertEquals(mensaje, exception.getMessage());
        assertEquals(causa, exception.getCause());
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    @DisplayName("Debe crear excepción con solo causa")
    void testConstructorConCausa() {
        // Given
        Throwable causa = new RuntimeException("Error al validar token");

        // When
        TokenHasExpiredException exception = new TokenHasExpiredException(causa);

        // Then
        assertNotNull(exception);
        assertEquals("El token ha expirado", exception.getMessage());
        assertEquals(causa, exception.getCause());
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("Error al validar token", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Debe ser una RuntimeException")
    void testEsRuntimeException() {
        // When
        TokenHasExpiredException exception = new TokenHasExpiredException();

        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada")
    void testLanzarYCapturar() {
        // When & Then
        assertThrows(TokenHasExpiredException.class, () -> {
            throw new TokenHasExpiredException("Token expirado en prueba");
        });
    }

    @Test
    @DisplayName("Debe mantener el serialVersionUID")
    void testSerialVersionUID() throws NoSuchFieldException {
        // When
        java.lang.reflect.Field field = TokenHasExpiredException.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);

        // Then
        assertNotNull(field);
        assertEquals(long.class, field.getType());
    }

    @Test
    @DisplayName("Debe poder capturar como RuntimeException genérica")
    void testCapturarComoRuntimeException() {
        // Given
        String mensajeEsperado = "Mensaje de prueba";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            throw new TokenHasExpiredException(mensajeEsperado);
        });

        assertInstanceOf(TokenHasExpiredException.class, exception);
        assertEquals(mensajeEsperado, exception.getMessage());
    }

    @Test
    @DisplayName("Debe preservar stack trace cuando se crea con causa")
    void testStackTrace() {
        // Given
        Exception causaOriginal = new Exception("Causa raíz");
        
        // When
        TokenHasExpiredException exception = new TokenHasExpiredException("Token expirado", causaOriginal);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
        assertEquals(causaOriginal, exception.getCause());
    }
}