package com.esimedia.features.content.dto;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TokenDTOTest {

    @Test
    void testNoArgsConstructor() {
        TokenDTO token = new TokenDTO();
        assertNotNull(token);
        assertNull(token.getId());
        assertNull(token.getIpCliente());
        assertNull(token.getEmailUsuario());
    }

    @Test
    void testGettersAndSetters() {
        TokenDTO token = new TokenDTO();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(1);

        token.setId("token-123");
        token.setIpCliente("192.168.1.1");
        token.setEmailUsuario("user@example.com");
        token.setFechaInicio(now);
        token.setFechaUltimaActividad(later);
        token.setEstado(EstadoToken.REVOCADA);
        token.setJwtTokenId("jwt-456");
        token.setTipoToken(TipoToken.ACCESO);
        token.setFechaCreacionToken(now);

        assertEquals("token-123", token.getId());
        assertEquals("192.168.1.1", token.getIpCliente());
        assertEquals("user@example.com", token.getEmailUsuario());
        assertEquals(now, token.getFechaInicio());
        assertEquals(later, token.getFechaUltimaActividad());
        assertEquals(EstadoToken.REVOCADA, token.getEstado());
        assertEquals("jwt-456", token.getJwtTokenId());
        assertEquals(TipoToken.ACCESO, token.getTipoToken());
        assertEquals(now, token.getFechaCreacionToken());
    }

    @Test
    void testBuilderBasic() {
        LocalDateTime now = LocalDateTime.now();
        
        TokenDTO token = TokenDTO.builder()
                .id("token-123")
                .ipCliente("192.168.1.1")
                .emailUsuario("user@example.com")
                .fechaInicio(now)
                .fechaUltimaActividad(now)
                .estado(EstadoToken.REVOCADA)
                .jwtTokenId("jwt-456")
                .tipoToken(TipoToken.ACCESO)
                .build();

        assertEquals("token-123", token.getId());
        assertEquals("192.168.1.1", token.getIpCliente());
        assertEquals("user@example.com", token.getEmailUsuario());
        assertEquals(now, token.getFechaInicio());
        assertEquals(now, token.getFechaUltimaActividad());
        assertEquals(EstadoToken.REVOCADA, token.getEstado());
        assertEquals("jwt-456", token.getJwtTokenId());
        assertEquals(TipoToken.ACCESO, token.getTipoToken());
    }

    @Test
    void testBuilderTokenRegistro() {
        TokenDTO token = TokenDTO.builder()
                .tokenRegistro("jwt-789", "10.0.0.1", "newuser@example.com")
                .build();

        assertEquals(TipoToken.REGISTRO, token.getTipoToken());
        assertEquals(EstadoToken.SIN_CONFIRMAR, token.getEstado());
        assertEquals("jwt-789", token.getJwtTokenId());
        assertEquals("10.0.0.1", token.getIpCliente());
        assertEquals("newuser@example.com", token.getEmailUsuario());
        assertNotNull(token.getFechaInicio());
        assertNotNull(token.getFechaUltimaActividad());
    }

    @Test
    void testBuilderTokenRecuperacion() {
        TokenDTO token = TokenDTO.builder()
                .tokenRecuperacion("jwt-999", "172.16.0.1", "forgot@example.com")
                .build();

        assertEquals(TipoToken.RECUPERACION_PASSWORD, token.getTipoToken());
        assertEquals(EstadoToken.SIN_CONFIRMAR, token.getEstado());
        assertEquals("jwt-999", token.getJwtTokenId());
        assertEquals("172.16.0.1", token.getIpCliente());
        assertEquals("forgot@example.com", token.getEmailUsuario());
        assertNotNull(token.getFechaInicio());
        assertNotNull(token.getFechaUltimaActividad());
    }

    @Test
    void testBuilderTokenAcceso() {
        TokenDTO token = TokenDTO.builder()
                .tokenAcceso("jwt-111", "192.168.0.100", "login@example.com")
                .build();

        assertEquals(TipoToken.ACCESO, token.getTipoToken());
        assertEquals(EstadoToken.SIN_CONFIRMAR, token.getEstado());
        assertEquals("jwt-111", token.getJwtTokenId());
        assertEquals("192.168.0.100", token.getIpCliente());
        assertEquals("login@example.com", token.getEmailUsuario());
        assertNotNull(token.getFechaInicio());
        assertNotNull(token.getFechaUltimaActividad());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        
        TokenDTO token1 = TokenDTO.builder()
                .id("token-123")
                .ipCliente("192.168.1.1")
                .emailUsuario("user@example.com")
                .fechaInicio(now)
                .estado(EstadoToken.REVOCADA)
                .jwtTokenId("jwt-456")
                .tipoToken(TipoToken.ACCESO)
                .build();

        TokenDTO token2 = TokenDTO.builder()
                .id("token-123")
                .ipCliente("192.168.1.1")
                .emailUsuario("user@example.com")
                .fechaInicio(now)
                .estado(EstadoToken.REVOCADA)
                .jwtTokenId("jwt-456")
                .tipoToken(TipoToken.ACCESO)
                .build();

        TokenDTO token3 = TokenDTO.builder()
                .id("token-999")
                .ipCliente("10.0.0.1")
                .emailUsuario("other@example.com")
                .build();

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
        assertNotEquals(token1, token3);
        assertNotEquals(token1.hashCode(), token3.hashCode());
    }

    @Test
    void testEqualsSameObject() {
        TokenDTO token = TokenDTO.builder().id("token-123").build();
        assertEquals(token, token);
    }

    @Test
    void testEqualsNull() {
        TokenDTO token = TokenDTO.builder().id("token-123").build();
        assertNotEquals(null, token);
    }

    @Test
    void testEqualsDifferentClass() {
        TokenDTO token = TokenDTO.builder().id("token-123").build();
        assertNotEquals("not a TokenDTO", token);
    }

    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        
        TokenDTO token = TokenDTO.builder()
                .id("token-123")
                .ipCliente("192.168.1.1")
                .emailUsuario("user@example.com")
                .fechaInicio(now)
                .estado(EstadoToken.REVOCADA)
                .jwtTokenId("jwt-456")
                .tipoToken(TipoToken.ACCESO)
                .build();

        String toString = token.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("token-123"));
        assertTrue(toString.contains("192.168.1.1"));
        assertTrue(toString.contains("user@example.com"));
        assertTrue(toString.contains("jwt-456"));
    }

    @Test
    void testBuilderChaining() {
        LocalDateTime now = LocalDateTime.now();
        
        TokenDTO token = TokenDTO.builder()
                .id("chain-1")
                .ipCliente("192.168.1.1")
                .emailUsuario("chain@example.com")
                .fechaInicio(now)
                .fechaUltimaActividad(now)
                .estado(EstadoToken.REVOCADA)
                .jwtTokenId("jwt-chain")
                .tipoToken(TipoToken.REGISTRO)
                .build();

        assertNotNull(token);
        assertEquals("chain-1", token.getId());
        assertEquals(TipoToken.REGISTRO, token.getTipoToken());
    }

    @Test
    void testBuilderWithNullValues() {
        TokenDTO token = TokenDTO.builder()
                .id(null)
                .ipCliente(null)
                .emailUsuario(null)
                .build();

        assertNull(token.getId());
        assertNull(token.getIpCliente());
        assertNull(token.getEmailUsuario());
    }

    @Test
    void testEqualsWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(1);
        
        TokenDTO token1 = new TokenDTO();
        token1.setId("id-1");
        token1.setIpCliente("192.168.1.1");
        token1.setEmailUsuario("user@example.com");
        token1.setFechaInicio(now);
        token1.setFechaUltimaActividad(later);
        token1.setEstado(EstadoToken.REVOCADA);
        token1.setJwtTokenId("jwt-1");
        token1.setTipoToken(TipoToken.ACCESO);
        token1.setFechaCreacionToken(now);

        TokenDTO token2 = new TokenDTO();
        token2.setId("id-1");
        token2.setIpCliente("192.168.1.1");
        token2.setEmailUsuario("user@example.com");
        token2.setFechaInicio(now);
        token2.setFechaUltimaActividad(later);
        token2.setEstado(EstadoToken.REVOCADA);
        token2.setJwtTokenId("jwt-1");
        token2.setTipoToken(TipoToken.ACCESO);
        token2.setFechaCreacionToken(now);

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    void testEqualsWithDifferentFields() {
        TokenDTO token1 = TokenDTO.builder()
                .id("id-1")
                .ipCliente("192.168.1.1")
                .build();

        TokenDTO token2 = TokenDTO.builder()
                .id("id-1")
                .ipCliente("10.0.0.1")
                .build();

        assertNotEquals(token1, token2);
    }
}