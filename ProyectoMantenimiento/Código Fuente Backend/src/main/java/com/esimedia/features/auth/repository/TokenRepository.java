package com.esimedia.features.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;
import com.esimedia.features.auth.entity.Token;
@Repository
public interface TokenRepository extends MongoRepository<Token, String> {

    /**
     * Busca un token por su jwtTokenId
     */
    Optional<Token> findByjwtTokenId(@Param("jwtTokenId") String jwtTokenId);

    /**
     * Busca tokens por email de usuario
     */
    List<Token> findByUsuarioEmail(String usuarioEmail);

    /**
     * Busca tokens por tipo
     */
    List<Token> findByTipoToken(TipoToken tipoToken);

    /**
     * Busca tokens por estado
     */
    List<Token> findByEstado(EstadoToken estado);

    /**
     * Busca tokens activos por email de usuario
     */
    List<Token> findByUsuarioEmailAndEstado(String usuarioEmail, EstadoToken estado);

    /**
     * Busca tokens por IP cliente
     */
    List<Token> findByIpCliente(String ipCliente);

    /**
     * Busca tokens expirados (fecha de última actividad antes de una fecha específica)
     */
    List<Token> findByFechaUltimaActividadBefore(LocalDateTime fecha);

    /**
     * Busca tokens por tipo y estado
     */
    List<Token> findByTipoTokenAndEstado(TipoToken tipoToken, EstadoToken estado);

    /**
     * Elimina tokens expirados
     */
    void deleteByFechaUltimaActividadBefore(LocalDateTime fecha);

    /**
     * Elimina tokens por estado
     */
    void deleteByEstado(EstadoToken estado);

    /**
     * Elimina tokens con tokenCreado null
     */
    long deleteByTokenCreadoIsNull();
}