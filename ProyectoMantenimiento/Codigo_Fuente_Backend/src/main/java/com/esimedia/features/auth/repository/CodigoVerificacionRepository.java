package com.esimedia.features.auth.repository;

import com.esimedia.features.auth.enums.TipoVerificacion;
import com.esimedia.features.auth.entity.CodigoVerificacion;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodigoVerificacionRepository extends MongoRepository<CodigoVerificacion, String> {
    
    /**
     * Busca un código específico por usuario, código y tipo
     */
    Optional<CodigoVerificacion> findByUserIdAndCodigoAndTipo(String userId, String codigo, TipoVerificacion tipo);
    
    /**
     * Busca códigos no usados por usuario y tipo
     */
    List<CodigoVerificacion> findByUserIdAndTipoAndUsadoFalse(String userId, TipoVerificacion tipo);
    
    /**
     * Busca códigos expirados
     */
    List<CodigoVerificacion> findByFechaExpiracionBefore(LocalDateTime fecha);
    
    /**
     * Elimina códigos expirados (para limpieza automática)
     */
    void deleteByFechaExpiracionBefore(LocalDateTime fecha);
    
    /**
     * Busca todos los códigos de un usuario
     */
    List<CodigoVerificacion> findByUserId(String userId);
    
    /**
     * Busca códigos por tipo
     */
    List<CodigoVerificacion> findByTipo(TipoVerificacion tipo);
}
