package com.esimedia.features.auth.entity;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    @Test
    void testTokenBuilder_FullConstruction() {
        // Arrange
        String id = "token123";
        String tokenCreado = "test-token-full-construction";
        String ipCliente = "192.168.1.1";
        LocalDateTime fechaInicio = LocalDateTime.now();
        LocalDateTime fechaUltimaActividad = LocalDateTime.now().plusMinutes(30);
        EstadoToken estado = EstadoToken.SIN_CONFIRMAR;
        String jwtTokenId = "jwt123";
        TipoToken tipoToken = TipoToken.CONFIRMACION_EMAIL;
        String usuarioEmail = "test@example.com";

        // Act
        Token token = Token.builder()
                .id(id)
                .tokenCreado(tokenCreado)
                .ipCliente(ipCliente)
                .fechaInicio(fechaInicio)
                .fechaUltimaActividad(fechaUltimaActividad)
                .estado(estado)
                .jwtTokenId(jwtTokenId)
                .tipoToken(tipoToken)
                .usuarioEmail(usuarioEmail)
                .build();

        // Assert
        assertEquals(id, token.getId());
        assertEquals(ipCliente, token.getIpCliente());
        assertEquals(fechaInicio, token.getFechaInicio());
        assertEquals(fechaUltimaActividad, token.getFechaUltimaActividad());
        assertEquals(estado, token.getEstado());
        assertEquals(jwtTokenId, token.getJwtTokenId());
        assertEquals(tipoToken, token.getTipoToken());
        assertEquals(usuarioEmail, token.getUsuarioEmail());
    }

    @Test
    void testTokenBuilder_PartialConstruction() {
        // Arrange & Act
        Token token = Token.builder()
                .tokenCreado("test-token-partial")
                .jwtTokenId("jwt456")
                .estado(EstadoToken.UTILIZADA)
                .usuarioEmail("user@test.com")
                .build();

        // Assert
        assertEquals("jwt456", token.getJwtTokenId());
        assertEquals(EstadoToken.UTILIZADA, token.getEstado());
        assertEquals("user@test.com", token.getUsuarioEmail());
        assertNull(token.getId());
        assertNull(token.getIpCliente());
        assertNull(token.getFechaInicio());
        assertNull(token.getFechaUltimaActividad());
        assertNull(token.getTipoToken());
    }

    @Test
    void testTokenDefaultConstructor() {
        // Act
        Token token = new Token();

        // Assert
        assertNull(token.getId());
        assertNull(token.getIpCliente());
        assertNull(token.getFechaInicio());
        assertNull(token.getFechaUltimaActividad());
        assertNull(token.getEstado());
        assertNull(token.getJwtTokenId());
        assertNull(token.getTipoToken());
        assertNull(token.getUsuarioEmail());
    }

    @Test
    void testTokenSettersAndGetters() {
        // Arrange
        Token token = new Token();
        String id = "test-id";
        String ipCliente = "10.0.0.1";
        LocalDateTime fechaInicio = LocalDateTime.now();
        LocalDateTime fechaUltimaActividad = LocalDateTime.now().plusHours(1);
        EstadoToken estado = EstadoToken.SIN_CONFIRMAR;
        String jwtTokenId = "jwt-test";
        TipoToken tipoToken = TipoToken.RECUPERACION_PASSWORD;
        String usuarioEmail = "setter@test.com";

        // Act
        token.setId(id);
        token.setIpCliente(ipCliente);
        token.setFechaInicio(fechaInicio);
        token.setFechaUltimaActividad(fechaUltimaActividad);
        token.setEstado(estado);
        token.setJwtTokenId(jwtTokenId);
        token.setTipoToken(tipoToken);
        token.setUsuarioEmail(usuarioEmail);

        // Assert
        assertEquals(id, token.getId());
        assertEquals(ipCliente, token.getIpCliente());
        assertEquals(fechaInicio, token.getFechaInicio());
        assertEquals(fechaUltimaActividad, token.getFechaUltimaActividad());
        assertEquals(estado, token.getEstado());
        assertEquals(jwtTokenId, token.getJwtTokenId());
        assertEquals(tipoToken, token.getTipoToken());
        assertEquals(usuarioEmail, token.getUsuarioEmail());
    }

    @Test
    void testTokenEquals_SameObject() {
        // Arrange
        Token token = Token.builder()
                .id("same-id")
                .tokenCreado("test-token-same")
                .jwtTokenId("jwt123")
                .build();

        // Act & Assert
        assertEquals(token, token);
    }

    @Test
    void testTokenEquals_EqualObjects() {
        // Arrange
        Token token1 = Token.builder()
                .id("same-id")
                .tokenCreado("test-token-equal")
                .jwtTokenId("jwt123")
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();

        Token token2 = Token.builder()
                .id("same-id")
                .tokenCreado("test-token-equal")
                .jwtTokenId("jwt123")
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();

        // Act & Assert
        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    void testTokenEquals_DifferentObjects() {
        // Arrange
        Token token1 = Token.builder()
                .id("id1")
                .tokenCreado("test-token-diff1")
                .jwtTokenId("jwt123")
                .build();

        Token token2 = Token.builder()
                .id("id2")
                .tokenCreado("test-token-diff2")
                .jwtTokenId("jwt456")
                .build();

        // Act & Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void testTokenEquals_NullAndDifferentClass() {
        // Arrange
        Token token = Token.builder()
                .id("test-id")
                .tokenCreado("test-token-null-check")
                .build();

        // Act & Assert
        assertNotEquals("not-a-token", token);
        assertNotEquals(null, token);
    }

    @Test
    void testTokenToString() {
        // Arrange
        Token token = Token.builder()
                .id("test-id")
                .tokenCreado("test-token-string")
                .ipCliente("127.0.0.1")
                .estado(EstadoToken.UTILIZADA)
                .jwtTokenId("jwt-string-test")
                .usuarioEmail("string@test.com")
                .build();

        // Act
        String tokenString = token.toString();

        // Assert
        assertNotNull(tokenString);
        assertTrue(tokenString.contains("test-id"));
        assertTrue(tokenString.contains("127.0.0.1"));
        assertTrue(tokenString.contains("UTILIZADA"));
        assertTrue(tokenString.contains("jwt-string-test"));
    }

    @Test
    void testTokenBuilder_FluentInterface() {
        // Act & Assert - Should not throw any exceptions
        Token token = Token.builder()
                .id("fluent-test")
                .tokenCreado("test-token-fluent")
                .ipCliente("192.168.0.1")
                .estado(EstadoToken.SIN_CONFIRMAR)
                .jwtTokenId("fluent-jwt")
                .tipoToken(TipoToken.CONFIRMACION_EMAIL)
                .usuarioEmail("fluent@test.com")
                .fechaInicio(LocalDateTime.now())
                .fechaUltimaActividad(LocalDateTime.now().plusMinutes(15))
                .build();

        assertNotNull(token);
        assertEquals("fluent-test", token.getId());
        assertEquals("fluent@test.com", token.getUsuarioEmail());
    }

    @Test
    void testTokenEstadoTransitions() {
        // Arrange
        Token token = new Token();

        // Act & Assert - Test different estado transitions
        token.setEstado(EstadoToken.SIN_CONFIRMAR);
        assertEquals(EstadoToken.SIN_CONFIRMAR, token.getEstado());

        token.setEstado(EstadoToken.UTILIZADA);
        assertEquals(EstadoToken.UTILIZADA, token.getEstado());
    }

    @Test
    void testTokenTipoTokenSettings() {
        // Arrange
        Token token = new Token();

        // Act & Assert - Test different tipo token settings
        token.setTipoToken(TipoToken.CONFIRMACION_EMAIL);
        assertEquals(TipoToken.CONFIRMACION_EMAIL, token.getTipoToken());

        token.setTipoToken(TipoToken.RECUPERACION_PASSWORD);
        assertEquals(TipoToken.RECUPERACION_PASSWORD, token.getTipoToken());
    }

    @Test
    void testTokenFechaOperations() {
        // Arrange
        Token token = new Token();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(2);

        // Act
        token.setFechaInicio(now);
        token.setFechaUltimaActividad(later);

        // Assert
        assertEquals(now, token.getFechaInicio());
        assertEquals(later, token.getFechaUltimaActividad());
        assertTrue(token.getFechaUltimaActividad().isAfter(token.getFechaInicio()));
    }
}