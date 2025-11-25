package com.esimedia.features.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.esimedia.features.auth.enums.EstadoSesion;
import com.esimedia.features.auth.entity.Sesion;

@Repository
public interface SesionRepository extends MongoRepository<Sesion, String> {
    
    // Buscar sesión por JWT token ID
    Optional<Sesion> findByJwtTokenId(String jwtTokenId);
    
    // Buscar sesiones por usuario
    List<Sesion> findByIdUsuario(String idUsuario);
    
    // Buscar sesiones activas por usuario
    List<Sesion> findByIdUsuarioAndEstado(String idUsuario, EstadoSesion estado);
    
    // Buscar todas las sesiones activas
    List<Sesion> findByEstado(EstadoSesion estado);
    
    // Buscar sesiones por IP cliente
    List<Sesion> findByIpCliente(String ipCliente);
    
    // Buscar sesiones que no han tenido actividad reciente (para expirar)
    List<Sesion> findByEstadoAndFechaUltimaActividadBefore(EstadoSesion estado, LocalDateTime fechaLimite);
    
    // Buscar sesiones con actividad anterior a una fecha (para limpieza)
    List<Sesion> findByFechaUltimaActividadBefore(LocalDateTime fechaLimite);
    
    // Eliminar sesiones por usuario
    void deleteByIdUsuario(String idUsuario);
    
    // Contar sesiones activas por usuario
    long countByIdUsuarioAndEstado(String idUsuario, EstadoSesion estado);
    
    // Buscar sesión por token de recuperación
    Optional<Sesion> findByResetToken(String resetToken);
}