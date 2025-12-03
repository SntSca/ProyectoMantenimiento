package com.esimedia.features.user_management.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.shared.util.JwtUtil;

import io.jsonwebtoken.JwtException;

/**
 * Servicio especializado para operaciones de búsqueda y recuperación de usuarios.
 * Se encarga de buscar usuarios por diferentes criterios (ID, email, alias) 
 * y extraer información de usuarios desde tokens JWT.
 */
@Service
public class UserRetrievalService {

    private static final String MESSAGE_USER_NOT_FOUND = "El usuario no ha podido ser encontrado";
    
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final Logger logger;

    public UserRetrievalService(UsuarioNormalRepository usuarioNormalRepository,
                               CreadorContenidoRepository creadorContenidoRepository,
                               AdminRepository adminRepository,
                               JwtUtil jwtUtil) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
        this.logger = LoggerFactory.getLogger(UserRetrievalService.class);
    }

    /**
     * Busca un usuario normal por email
     */
    public Optional<UsuarioNormal> findByEmail(String email) {
        logger.debug("[FIND_BY_EMAIL] Buscando usuario normal por email: {}", email);
        return usuarioNormalRepository.findByemail(email);
    }

    /**
     * Busca un usuario normal por alias
     */
    public Optional<UsuarioNormal> findByAlias(String alias) {
        logger.debug("[FIND_BY_ALIAS] Buscando usuario normal por alias: {}", alias);
        return usuarioNormalRepository.findByalias(alias);
    }

    /**
     * Busca un usuario normal por ID
     */
    public Optional<UsuarioNormal> findById(String id) {
        logger.debug("[FIND_BY_ID] Buscando usuario normal por ID: {}", id);
        return usuarioNormalRepository.findById(id);
    }

    /**
     * Busca cualquier tipo de usuario por ID
     */
    public Optional<Usuario> findAnyUserById(String id) {
        logger.debug("[FIND_ANY_USER_BY_ID] Buscando cualquier usuario por ID: {}", id);
        Optional<Usuario> usuarioOpt = Optional.empty();
        // Buscar en usuarios normales
        Optional<UsuarioNormal> usuarioNormal = usuarioNormalRepository.findById(id);
        if (usuarioNormal.isPresent()) {
            usuarioOpt = Optional.of(usuarioNormal.get());
        }

        // Buscar en creadores de contenido
        Optional<CreadorContenido> creador = creadorContenidoRepository.findById(id);
        if (creador.isPresent()) {
            usuarioOpt = Optional.of(creador.get());
        }

        // Buscar en administradores
        Optional<Administrador> admin = adminRepository.findById(id);
        if (admin.isPresent()) {
            usuarioOpt = Optional.of(admin.get());
        }

        return usuarioOpt;
    }

    /**
     * Busca cualquier tipo de usuario por alias
     */
    public Optional<Usuario> findAnyUserByAlias(String alias) {
        logger.debug("[FIND_ANY_USER_BY_ALIAS] Buscando cualquier usuario por alias: {}", alias);
        Optional<Usuario> usuarioOpt = Optional.empty();
        
        // Buscar en usuarios normales
        Optional<UsuarioNormal> usuarioNormal = usuarioNormalRepository.findByalias(alias);
        if (usuarioNormal.isPresent()) {
            usuarioOpt = Optional.of(usuarioNormal.get());
        }

        // Buscar en creadores de contenido
        if (usuarioOpt.isEmpty()) {
            Optional<CreadorContenido> creador = creadorContenidoRepository.findByAlias(alias);
            if (creador.isPresent()) {
                usuarioOpt = Optional.of(creador.get());
            }
        }

        // Buscar en administradores
        if (usuarioOpt.isEmpty()) {
            Optional<Administrador> admin = adminRepository.findByalias(alias);
            if (admin.isPresent()) {
                usuarioOpt = Optional.of(admin.get());
            }
        }

        return usuarioOpt;
    }

    /**
     * Busca cualquier tipo de usuario por email
     */
    public Optional<Usuario> findAnyUserByEmail(String email) {
        logger.debug("[FIND_ANY_USER_BY_EMAIL] Buscando cualquier usuario por email: {}", email);
        Optional<Usuario> usuarioOpt = Optional.empty();
        
        // Buscar en usuarios normales
        Optional<UsuarioNormal> usuarioNormal = usuarioNormalRepository.findByemail(email);
        if (usuarioNormal.isPresent()) {
            usuarioOpt = Optional.of(usuarioNormal.get());
        }

        // Buscar en creadores de contenido
        if (usuarioOpt.isEmpty()) {
            Optional<CreadorContenido> creador = creadorContenidoRepository.findByemail(email);
            if (creador.isPresent()) {
                usuarioOpt = Optional.of(creador.get());
            }
        }

        // Buscar en administradores
        if (usuarioOpt.isEmpty()) {
            Optional<Administrador> admin = adminRepository.findByemail(email);
            if (admin.isPresent()) {
                usuarioOpt = Optional.of(admin.get());
            }
        }

        return usuarioOpt;
    }

    /**
     * Extrae el token del header Authorization, obtiene el subject (userId) y devuelve
     * el objeto Usuario correspondiente buscando por id en las distintas colecciones.
     * Lanza ResponseStatusException con 401 si el header/token no son válidos o 404 si no se encuentra el usuario.
     */
    public Usuario getUserFromAuthHeader(String authHeader) {
        logger.debug("[GET_USER_FROM_AUTH_HEADER] Extrayendo usuario desde header de autorización");
        
        try {
            String token = authHeader.substring(7);
            String userId = jwtUtil.getUserIdFromToken(token);

            // Intentar usuario normal
            if (usuarioNormalRepository.existsById(userId)) {
                return usuarioNormalRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_USER_NOT_FOUND));
            }

            // Intentar creador de contenido
            if (creadorContenidoRepository.existsById(userId)) {
                return creadorContenidoRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_USER_NOT_FOUND));
            }

            // Intentar administrador
            if (adminRepository.existsById(userId)) {
                return adminRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_USER_NOT_FOUND));
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_USER_NOT_FOUND);
        }
        catch (IllegalArgumentException | JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autorización inválido", e);
        }
    }
}