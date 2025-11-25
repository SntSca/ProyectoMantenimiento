package com.esimedia.features.auth.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;
import com.esimedia.features.auth.entity.Token;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.TokenRepository;
import com.esimedia.features.content.dto.TokenDTO;
import com.esimedia.shared.exception.TokenHasExpiredException;

/**
 * Servicio para gestión de tokens
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final long TOKEN_EXPIRATION_HOURS = 24;

    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Busca un token por JWT Token ID
     * @param jwtTokenId ID del JWT
     * @return Optional con el TokenDTO si existe
     */
    public Optional<TokenDTO> findByJwtTokenId(String jwtTokenId) {
        Optional<Token> token = tokenRepository.findByjwtTokenId(jwtTokenId);
        return token.map(t -> TokenDTO.builder()
                .id(t.getId())
                .emailUsuario(t.getUsuarioEmail())
                .fechaInicio(t.getFechaInicio())
                .fechaUltimaActividad(t.getFechaUltimaActividad())
                .ipCliente(t.getIpCliente())
                .jwtTokenId(t.getJwtTokenId())
                .tipoToken(t.getTipoToken())
                .estado(t.getEstado())
                .build());
    }

    /**
     * Busca tokens por email
     * @param email Email del usuario
     * @return Lista de TokenDTO
     */
    public List<TokenDTO> findByEmail(String email) {
        List<Token> tokens = tokenRepository.findByUsuarioEmail(email);
        return tokens.stream()
                .map(t -> TokenDTO.builder()
                    .id(t.getId())
                    .emailUsuario(t.getUsuarioEmail())
                    .fechaInicio(t.getFechaInicio())
                    .fechaUltimaActividad(t.getFechaUltimaActividad())
                    .ipCliente(t.getIpCliente())
                    .jwtTokenId(t.getJwtTokenId())
                    .tipoToken(t.getTipoToken())
                    .estado(t.getEstado())
                    .build())
                    .toList();
    }

    /**
     * Valida si un token ha expirado
     * @param tokenDTO Token a validar
     * @throws TokenHasExpiredException si el token ha expirado
     */
    public void validateTokenExpiration(TokenDTO tokenDTO) {
        if (tokenDTO.getFechaCreacionToken() != null) {
            LocalDateTime expirationTime = tokenDTO.getFechaCreacionToken().plusHours(TOKEN_EXPIRATION_HOURS);
            if (LocalDateTime.now().isAfter(expirationTime)) {
                logger.warn("Token expirado: {}", tokenDTO.getId());
                throw new TokenHasExpiredException("El token ha expirado después de " + TOKEN_EXPIRATION_HOURS + " horas");
            }
        }
    }

    /**
     * Revoca un token
     * @param tokenId ID del token
     */
    public void revokeToken(String tokenId) {
        Optional<Token> tokenOpt = tokenRepository.findById(tokenId);
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            token.setEstado(EstadoToken.REVOCADA);
            token.setFechaUltimaActividad(LocalDateTime.now());
            tokenRepository.save(token);
            logger.info("Token revocado: {}", tokenId);
        }
    }

    /**
     * Elimina un token
     * @param tokenId ID del token
     */
    public void deleteToken(String tokenId) {
        tokenRepository.deleteById(tokenId);
        logger.info("Token eliminado: {}", tokenId);
    }

    /**
     * Limpia tokens expirados
     * @return Número de tokens eliminados
     */
    public long cleanExpiredTokens() {
        LocalDateTime expirationDate = LocalDateTime.now().minusHours(TOKEN_EXPIRATION_HOURS);
        List<Token> expiredTokens = tokenRepository.findByFechaUltimaActividadBefore(expirationDate);
        
        long deletedCount = expiredTokens.size();
        tokenRepository.deleteByFechaUltimaActividadBefore(expirationDate);
        
        logger.info("Limpieza de tokens expirados completada. Eliminados: {}", deletedCount);
        return deletedCount;
    }

    /**
     * Actualiza la última actividad de un token
     * @param tokenId ID del token
     */
    public void updateTokenActivity(String tokenId) {
        Optional<Token> tokenOpt = tokenRepository.findById(tokenId);
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            token.setFechaUltimaActividad(LocalDateTime.now());
            tokenRepository.save(token);
        }
    }

    public void saveToken(String token, UsuarioNormal usuario) {
        Token newToken = new Token();
        newToken.setJwtTokenId(token);
        newToken.setUsuarioEmail(usuario.getEmail());
        newToken.setTipoToken(TipoToken.CONFIRMACION_CUENTA);
        newToken.setFechaInicio(LocalDateTime.now());
        newToken.setFechaUltimaActividad(LocalDateTime.now());
        newToken.setEstado(EstadoToken.SIN_CONFIRMAR);
        tokenRepository.save(newToken);
        logger.info("Nuevo token guardado para el usuario: {}", usuario.getEmail());
    }
}