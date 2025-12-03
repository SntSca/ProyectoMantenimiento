package com.esimedia.features.auth.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.enums.EstadoSesion;
import com.esimedia.features.auth.entity.Sesion;
import com.esimedia.features.auth.repository.SesionRepository;

/**
 * Servicio para gestión de sesiones de usuario
 */
@Service
public class SesionService {

    private static final Logger logger = LoggerFactory.getLogger(SesionService.class);
    private static final int MAX_SESIONES_POR_USUARIO = 5; 

    private final SesionRepository sesionRepository;

    public SesionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    /**
     * Crea una nueva sesión para un usuario
     */
    public Sesion crearSesion(String idUsuario, String ipCliente, String jwtTokenId) {
        logger.info("Creando nueva sesión para usuario: {} desde IP: {}", idUsuario, ipCliente);

        // Verificar límite de sesiones activas
        List<Sesion> sesionesActivas = sesionRepository.findByIdUsuarioAndEstado(idUsuario, EstadoSesion.ACTIVA);
        if (sesionesActivas.size() >= MAX_SESIONES_POR_USUARIO) {
            Sesion sesionMasAntigua = sesionesActivas.stream()
                .min((s1, s2) -> s1.getFechaInicio().compareTo(s2.getFechaInicio()))
                .orElse(null);
            if (sesionMasAntigua != null) {
                eliminarSesion(sesionMasAntigua.getIdSesion());
                logger.info("Expirada sesión antigua para usuario: {}", idUsuario);
                sesionRepository.delete(sesionMasAntigua);
            }
        }

        Sesion nuevaSesion = new Sesion();
        nuevaSesion.setIdUsuario(idUsuario);
        nuevaSesion.setIpCliente(ipCliente);
        nuevaSesion.setJwtTokenId(jwtTokenId);
        nuevaSesion.setEstado(EstadoSesion.ACTIVA);
        nuevaSesion.setFechaInicio(LocalDateTime.now());
        nuevaSesion.setFechaUltimaActividad(LocalDateTime.now());
        nuevaSesion.setIdSesion(UUID.randomUUID().toString());
        sesionRepository.save(nuevaSesion);
        logger.info("Sesión creada exitosamente: {}", nuevaSesion.getIdSesion());
        return nuevaSesion;
    }

    /**
     * Busca una sesión por su ID
     */
    public Optional<Sesion> findById(String idSesion) {
        return sesionRepository.findById(idSesion);
    }

    /**
     * Valida una sesión (activa, no expirada, IP coincide)
     */
    public boolean validarSesion(String jwtTokenId, String ipCliente) {
        Optional<Sesion> sesionOpt = sesionRepository.findByJwtTokenId(jwtTokenId);
        boolean found = true;
        if (!sesionOpt.isPresent()) {
            logger.warn("Sesión no encontrada para JWT token ID: {}", jwtTokenId);
            found = false;
        } 
        else {

            Sesion sesion = sesionOpt.get();

            // Verificar estado
            if (!sesion.isActiva()) {
                logger.warn("Sesión inactiva: {}", sesion.getIdSesion());
                found = false;
            }

            // Verificar IP (protección contra robo de sesión)
            if (!sesion.getIpCliente().equals(ipCliente)) {
                logger.warn("IP no coincide para sesión {}: esperado {}, recibido {}", 
                        sesion.getIdSesion(), sesion.getIpCliente(), ipCliente);
                // Bloquear la sesión por posible robo
                eliminarSesion(sesion.getIdSesion());
                found = false;
            }

            // Verificar expiración (24 horas de inactividad)
            if (sesion.getFechaUltimaActividad().isBefore(LocalDateTime.now().minusHours(24))) {
                logger.warn("Sesión expirada por inactividad: {}", sesion.getIdSesion());
                eliminarSesion(sesion.getIdSesion());
                found = false;
            }
        }
        return found;
    }

    /**
     * Expira una sesión
     */
    public void eliminarSesion(String idSesion) {
        Optional<Sesion> sesionOpt = sesionRepository.findById(idSesion);
        if (sesionOpt.isPresent()) {
            sesionRepository.delete(sesionOpt.get());
        }
    }

    /**
     * Obtiene todas las sesiones activas de un usuario
     */
    public List<Sesion> getSesionesActivasUsuario(String idUsuario) {
        return sesionRepository.findByIdUsuarioAndEstado(idUsuario, EstadoSesion.ACTIVA);
    }

    /**
     * Expira todas las sesiones de un usuario (útil para logout global o cambio de contraseña)
     */
    public void eliminarTodasSesionesUsuario(String idUsuario) {
        sesionRepository.deleteByIdUsuario(idUsuario);
        logger.info("Eliminadas todas las sesiones para usuario: {}", idUsuario);
    }

    /**
     * Rota sesión por fixation: expira todas las sesiones anteriores y crea nueva
     */
    public Sesion rotarSesionPorFixation(String idUsuario, String ipCliente, String jwtTokenId) {
        logger.info("Rotando sesión por fixation para usuario: {}", idUsuario);
        // Expirar todas las sesiones activas anteriores
        eliminarTodasSesionesUsuario(idUsuario);
        // Crear nueva sesión
        return crearSesion(idUsuario, ipCliente, jwtTokenId);
    }

    /**
     * Expira todas las sesiones activas (usado al cerrar la aplicación)
     */
    public void expirarTodasSesionesActivas() {
        List<Sesion> sesionesActivas = sesionRepository.findByEstado(EstadoSesion.ACTIVA);
        for (Sesion sesion : sesionesActivas) {
            sesion.expirarSesion();
            sesionRepository.save(sesion);
        }
        logger.info("Expiradas {} sesiones activas al cerrar la app.", sesionesActivas.size());
    }

    /**
     * Limpia sesiones expiradas e inválidas al iniciar la aplicación
     */
    public void limpiarSesionesExpiradasEInvalidas() {
        logger.info("Buscando sesiones para limpiar...");

        // Limpiar sesiones con jwtTokenId null
        List<Sesion> sesionesNull = sesionRepository.findAll().stream()
            .filter(s -> s.getJwtTokenId() == null)
            .toList();

        // Limpiar todas las sesiones previas del usuario
        List<Sesion> sesionesExpiradasPorEstado = sesionRepository.findByEstado(EstadoSesion.EXPIRADA);
        sesionesExpiradasPorEstado.addAll(sesionRepository.findByEstado(EstadoSesion.BLOQUEADA));
        
        // Limpiar sesiones expiradas por inactividad (>24 horas)
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Sesion> sesionesExpiradasPorTiempo = sesionRepository.findByFechaUltimaActividadBefore(cutoff)
            .stream()
            .filter(s -> !sesionesNull.contains(s) && !sesionesExpiradasPorEstado.contains(s))
            .toList();

        // Combinar todas para eliminar
        List<Sesion> sesionesParaEliminar = new ArrayList<>(sesionesNull);
        sesionesParaEliminar.addAll(sesionesExpiradasPorEstado);
        sesionesParaEliminar.addAll(sesionesExpiradasPorTiempo);

        if (!sesionesParaEliminar.isEmpty()) {
            sesionRepository.deleteAll(sesionesParaEliminar);
            logger.info("Sesiones limpiadas: {} con null, {} expiradas por estado, {} expiradas por tiempo, total: {}",
                       sesionesNull.size(), sesionesExpiradasPorEstado.size(), sesionesExpiradasPorTiempo.size(), sesionesParaEliminar.size());
        } 
		else {
            logger.info("No hay sesiones para limpiar.");
        }
    }

    public void limpiarTodasLasSesiones(String idUsuario) {
        logger.info("Buscando sesiones para limpiar...");
        sesionRepository.deleteByIdUsuario(idUsuario);
        logger.info("Sesiones limpiadas para el usuario: {}", idUsuario);

    }

    /**
     * Elimina la sesión asociada a un JWT específico (logout)
     */
    public boolean eliminarSesionPorJwt(String jwtTokenId) {
        Optional<Sesion> sesionOpt = sesionRepository.findByJwtTokenId(jwtTokenId);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            sesionRepository.delete(sesion);
            logger.info("Sesión eliminada para logout: {} del usuario: {}", sesion.getIdSesion(), sesion.getIdUsuario());
            return true;
        } 
		else {
            logger.warn("No se encontró sesión para eliminar con JWT: {}", jwtTokenId);
            return false;
        }
    }

    /**
     * Elimina una sesión específica por su ID
     */
    public boolean eliminarSesionPorId(String idSesion) {
        Optional<Sesion> sesionOpt = sesionRepository.findById(idSesion);
        if (sesionOpt.isPresent()) {
            Sesion sesion = sesionOpt.get();
            sesionRepository.delete(sesion);
            logger.info("Sesión eliminada por ID: {} del usuario: {}", sesion.getIdSesion(), sesion.getIdUsuario());
            return true;
        }
        else {
            logger.warn("No se encontró sesión para eliminar con ID: {}", idSesion);
            return false;
        }
    }
}