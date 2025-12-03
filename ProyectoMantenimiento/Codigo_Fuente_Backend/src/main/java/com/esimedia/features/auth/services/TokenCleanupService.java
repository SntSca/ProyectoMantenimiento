package com.esimedia.features.auth.services;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Sesion;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.EstadoSesion;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.SesionRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

import jakarta.transaction.Transactional;

@Service
public class TokenCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);

    private final SesionRepository sesionRepository;
    private final UsuarioNormalRepository usuarioRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;

    public TokenCleanupService(SesionRepository sesionRepository, UsuarioNormalRepository usuarioRepository, CreadorContenidoRepository creadorContenidoRepository) {
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
    }

    /**
     * Ejecutado todos los días a las 3:00 AM. Elimina sesiones expiradas (más de 24h)
     * y también elimina usuarios no validados si ya no tienen sesión activa.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredSessionsAndUnconfirmedUsers() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusHours(24);

        // Eliminar sesiones expiradas
        List<Sesion> sesionesExpiradas = sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            EstadoSesion.EXPIRADA, fechaLimite
        );
        int sesionesEliminadas = sesionesExpiradas.size();
        for (Sesion sesion : sesionesExpiradas) {
            sesionRepository.delete(sesion);
        }


        // Eliminar usuarios normales no validados sin sesiones activas
        List<UsuarioNormal> usuariosNormales = usuarioRepository.findAll();
        int usuariosEliminados = 0;
        for (UsuarioNormal usuario : usuariosNormales) {
            if (!usuario.isConfirmado()) {
                long sesionesActivas = sesionRepository.countByIdUsuarioAndEstado(usuario.getIdUsuario(), EstadoSesion.ACTIVA);
                if (sesionesActivas == 0) {
                    usuarioRepository.delete(usuario);
                    usuariosEliminados++;
                    logger.info("[TokenCleanup] Usuario normal no confirmado eliminado: {}", usuario.getAlias());
                }
            }
        }

        // Eliminar creadores de contenido no validados sin sesiones activas
        List<CreadorContenido> creadores = creadorContenidoRepository.findAll();
        for (CreadorContenido creador : creadores) {
            if (!creador.isValidado()) {
            long sesionesActivas = sesionRepository.countByIdUsuarioAndEstado(creador.getIdUsuario(), EstadoSesion.ACTIVA);
            if (sesionesActivas == 0) {
                creadorContenidoRepository.delete(creador);
                usuariosEliminados++;
                logger.info("[TokenCleanup] Creador de contenido no validado eliminado: {}", creador.getAlias());
            }
            }
        }

        if (sesionesEliminadas > 0 || usuariosEliminados > 0) {
        logger.info("[TokenCleanup] Eliminadas {} sesiones expiradas y {} usuarios no confirmados.", sesionesEliminadas, usuariosEliminados);
        }
    }
}