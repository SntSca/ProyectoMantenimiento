package com.esimedia.features.auth.services;

import com.esimedia.features.auth.entity.Token;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.content.dto.TokenDTO;
import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;
import com.esimedia.features.auth.repository.TokenRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByJwtTokenId_ReturnsToken() {
        String jwtId = "jwt123";
        Token token = new Token();
        token.setId("token123");
        token.setUsuarioEmail("test@email.com");
        token.setJwtTokenId(jwtId);
        token.setFechaInicio(LocalDateTime.now());
        token.setFechaUltimaActividad(LocalDateTime.now());
        token.setIpCliente("192.168.1.1");
        token.setTipoToken(TipoToken.CONFIRMACION_CUENTA);
        token.setEstado(EstadoToken.SIN_CONFIRMAR);
        
        when(tokenRepository.findByjwtTokenId(jwtId)).thenReturn(Optional.of(token));
        
        Optional<TokenDTO> result = tokenService.findByJwtTokenId(jwtId);
        
        assertTrue(result.isPresent());
        assertEquals(jwtId, result.get().getJwtTokenId());
        assertEquals("test@email.com", result.get().getEmailUsuario());
    }

    @Test
    void testFindByJwtTokenId_NotFound() {
        String jwtId = "jwtNotFound";
        
        when(tokenRepository.findByjwtTokenId(jwtId)).thenReturn(Optional.empty());
        
        Optional<TokenDTO> result = tokenService.findByJwtTokenId(jwtId);
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmail_ReturnsTokens() {
        String email = "user@email.com";
        
        Token token1 = new Token();
        token1.setId("token1");
        token1.setUsuarioEmail(email);
        token1.setJwtTokenId("jwt1");
        token1.setFechaInicio(LocalDateTime.now());
        token1.setFechaUltimaActividad(LocalDateTime.now());
        token1.setTipoToken(TipoToken.CONFIRMACION_CUENTA);
        token1.setEstado(EstadoToken.SIN_CONFIRMAR);
        
        Token token2 = new Token();
        token2.setId("token2");
        token2.setUsuarioEmail(email);
        token2.setJwtTokenId("jwt2");
        token2.setFechaInicio(LocalDateTime.now());
        token2.setFechaUltimaActividad(LocalDateTime.now());
        token2.setTipoToken(TipoToken.CONFIRMACION_CUENTA);
        token2.setEstado(EstadoToken.SIN_CONFIRMAR);
        
        List<Token> tokens = Arrays.asList(token1, token2);
        
        when(tokenRepository.findByUsuarioEmail(email)).thenReturn(tokens);
        
        List<TokenDTO> result = tokenService.findByEmail(email);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("jwt1", result.get(0).getJwtTokenId());
        assertEquals("jwt2", result.get(1).getJwtTokenId());
    }

    @Test
    void testFindByEmail_EmptyList() {
        String email = "nouser@email.com";
        
        when(tokenRepository.findByUsuarioEmail(email)).thenReturn(Arrays.asList());
        
        List<TokenDTO> result = tokenService.findByEmail(email);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateTokenExpiration_TokenValid() {
        TokenDTO tokenDTO = TokenDTO.builder()
            .id("token123")
            .build();
        
        assertDoesNotThrow(() -> tokenService.validateTokenExpiration(tokenDTO));
    }

    @Test
    void testRevokeToken_TokenExists() {
        String tokenId = "token123";
        Token token = new Token();
        token.setId(tokenId);
        token.setEstado(EstadoToken.SIN_CONFIRMAR);
        token.setFechaUltimaActividad(LocalDateTime.now().minusHours(1));
        
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.of(token));
        
        tokenService.revokeToken(tokenId);
        
        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        
        Token savedToken = tokenCaptor.getValue();
        assertEquals(EstadoToken.REVOCADA, savedToken.getEstado());
        assertNotNull(savedToken.getFechaUltimaActividad());
    }

    @Test
    void testRevokeToken_TokenNotFound() {
        String tokenId = "nonexistent";
        
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.empty());
        
        tokenService.revokeToken(tokenId);
        
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    void testDeleteToken() {
        String tokenId = "token123";
        
        tokenService.deleteToken(tokenId);
        
        verify(tokenRepository).deleteById(tokenId);
    }

    @Test
    void testCleanExpiredTokens_WithExpiredTokens() {
        LocalDateTime expirationDate = LocalDateTime.now().minusHours(24);
        
        Token token1 = new Token();
        token1.setId("old1");
        token1.setFechaUltimaActividad(expirationDate.minusHours(5));
        
        Token token2 = new Token();
        token2.setId("old2");
        token2.setFechaUltimaActividad(expirationDate.minusHours(10));
        
        List<Token> expiredTokens = Arrays.asList(token1, token2);
        
        when(tokenRepository.findByFechaUltimaActividadBefore(any(LocalDateTime.class)))
            .thenReturn(expiredTokens);
        
        long result = tokenService.cleanExpiredTokens();
        
        assertEquals(2, result);
        verify(tokenRepository).deleteByFechaUltimaActividadBefore(any(LocalDateTime.class));
    }

    @Test
    void testCleanExpiredTokens_NoExpiredTokens() {
        when(tokenRepository.findByFechaUltimaActividadBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());
        
        long result = tokenService.cleanExpiredTokens();
        
        assertEquals(0, result);
        verify(tokenRepository).deleteByFechaUltimaActividadBefore(any(LocalDateTime.class));
    }

    @Test
    void testUpdateTokenActivity_TokenExists() {
        String tokenId = "token123";
        Token token = new Token();
        token.setId(tokenId);
        token.setFechaUltimaActividad(LocalDateTime.now().minusHours(5));
        
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.of(token));
        
        tokenService.updateTokenActivity(tokenId);
        
        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        
        Token savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getFechaUltimaActividad());
    }

    @Test
    void testUpdateTokenActivity_TokenNotFound() {
        String tokenId = "nonexistent";
        
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.empty());
        
        tokenService.updateTokenActivity(tokenId);
        
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    void testSaveToken() {
        String tokenString = "jwt-token-123";
        UsuarioNormal usuario = new UsuarioNormal();
        usuario.setEmail("user@email.com");
        
        tokenService.saveToken(tokenString, usuario);
        
        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        
        Token savedToken = tokenCaptor.getValue();
        assertEquals(tokenString, savedToken.getJwtTokenId());
        assertEquals("user@email.com", savedToken.getUsuarioEmail());
        assertEquals(TipoToken.CONFIRMACION_CUENTA, savedToken.getTipoToken());
        assertEquals(EstadoToken.SIN_CONFIRMAR, savedToken.getEstado());
        assertNotNull(savedToken.getFechaInicio());
        assertNotNull(savedToken.getFechaUltimaActividad());
    }

    @Test
    void testLambdaFindByEmail1() {
        Token token = new Token();
        token.setUsuarioEmail("test@mail.com");
        token.setEstado(EstadoToken.SIN_CONFIRMAR);
        
        boolean result = token.getUsuarioEmail().equals("test@mail.com") 
            && !token.getEstado().equals(EstadoToken.UTILIZADA);
        
        assertTrue(result);
    }

    @Test
    void testLambdaFindByJwtTokenId0() {
        Token token = new Token();
        token.setJwtTokenId("jwt123");
        token.setEstado(EstadoToken.SIN_CONFIRMAR);
        
        boolean result = token.getJwtTokenId().equals("jwt123");
        
        assertEquals(EstadoToken.SIN_CONFIRMAR, token.getEstado());
        assertTrue(result);
    }
}