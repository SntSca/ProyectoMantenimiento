package com.esimedia.shared.util;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

/**
 * Servicio centralizado para validación de tokens JWT con reglas de negocio.
 * Unifica las validaciones comunes de autorización basadas en roles y tipos de contenido.
 */
@Service
public class JwtValidationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtValidationUtil.class);
    
    // Constantes para literales duplicados
    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    
    private final JwtUtil jwtUtil;
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository administradorRepository;
    public JwtValidationUtil(JwtUtil jwtUtil, 
                               UsuarioNormalRepository usuarioNormalRepository,
                               CreadorContenidoRepository creadorContenidoRepository,
                               AdminRepository administradorRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.administradorRepository = administradorRepository;
    }

    /**
     * Valida un token JWT con reglas de negocio específicas.
     * 
     * @param authHeader Header Authorization con JWT
     * @param allowedRoles Lista de roles permitidos para la operación
     * @param requiredTipoContenido Tipo de contenido requerido (null si no aplica)
     * @return Username del usuario validado
     * @throws ResponseStatusException si la validación falla
     */
    public String validateJwtWithBusinessRules(String authHeader, List<Rol> allowedRoles, TipoContenido requiredTipoContenido) {

        
        try {

            String userId = jwtUtil.getUserIdFromToken(authHeader);
            String rolString = jwtUtil.getRolFromToken(authHeader);

            // Convertir rol string a enum
            Rol userRole = Rol.valueOf(rolString);

            logger.debug("Validando token para usuario: {} con rol: {}", userId, userRole);
            // Verificar que el rol esté permitido
            if (!allowedRoles.contains(userRole)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "No tiene permisos para realizar esta operación");
            }

            // Validaciones específicas por tipo de usuario
            validateUserByRole(userId, userRole);

            return userId;
            
        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autorización inválido");
        }
    }
    
    /**
     * Valida reglas específicas según el rol del usuario.
     */
    private void validateUserByRole(String userId, Rol userRole) {
        switch (userRole) {
            case NORMAL:
                validateUsuarioNormal(userId);
                break;
                
            case CREADOR:
                validateCreadorContenido(userId);
                break;
            case ADMINISTRADOR:
                validateAdministrador(userId);
                logger.debug("Administrador {} validado correctamente", userId);
                break;
            default:
                logger.warn("Rol no reconocido: {}", userRole);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Rol de usuario no reconocido");
        }
    }
    
    /**
     * Valida que un usuario normal esté en la BD.
     */
    private void validateUsuarioNormal(String userId) {
        Optional<UsuarioNormal> usuarioOpt = usuarioNormalRepository.findById(userId);
        if (usuarioOpt.isEmpty()) {
            logger.warn("Usuario normal no encontrado: {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USUARIO_NO_ENCONTRADO);
        }
        logger.debug("Usuario normal {} validado correctamente", userId);
    }
    
    /**
     * Valida que un creador de contenido tenga el tipo correcto.
     */
    private void validateCreadorContenido(String userId) {
        Optional<CreadorContenido> creadorOpt = creadorContenidoRepository.findById(userId);
        if (creadorOpt.isEmpty()) {
            logger.warn("Creador de contenido no encontrado: {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USUARIO_NO_ENCONTRADO);
        }
        

        
        logger.debug("Creador de contenido {} validado correctamente", userId);
    }
    
    private void validateAdministrador(String userId) {
        //Miramos en su repository por si existiera algún administrador con las credenciales
        Optional<Administrador> adminOpt = administradorRepository.findById(userId);
        if (adminOpt.isEmpty()) {
            logger.warn("Administrador no encontrado: {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USUARIO_NO_ENCONTRADO);
        }
        logger.debug("Administrador {} validado correctamente", userId);
    }

    /**
     * Método de conveniencia para validaciones comunes de acceso a contenido.
     * Permite solo NORMAL para acceder a contenido.
     */
    public String validateContentAccess(String authHeader, TipoContenido tipoContenido) {
        List<Rol> allowedRoles = List.of(Rol.NORMAL, Rol.ADMINISTRADOR, Rol.CREADOR);
        return validateJwtWithBusinessRules(authHeader, allowedRoles, tipoContenido);
    }
    
    /**
     * Método de conveniencia para validaciones de subida de contenido.
     * Solo permite CREADOR del tipo especificado.
     */
    public String validateContentUpload(String authHeader, TipoContenido tipoContenido) {
        List<Rol> allowedRoles = List.of(Rol.CREADOR);
        logger.debug("Validando subida de contenido para tipo: {}", tipoContenido);
        return validateJwtWithBusinessRules(authHeader, allowedRoles, tipoContenido);
    }

    public String validarGenerico(String authHeader) {
        List<Rol> allowedRoles = List.of(Rol.CREADOR, Rol.NORMAL, Rol.ADMINISTRADOR);
        return validateJwtWithBusinessRules(authHeader, allowedRoles, null);
    }
    
    public String validarGetUsuario(String authHeader) {
        List<Rol> allowedRoles = List.of(Rol.ADMINISTRADOR, Rol.NORMAL);
        return validateJwtWithBusinessRules(authHeader, allowedRoles, null);
    }

    public String validarGetAdmin(String authHeader) {
        List<Rol> allowedRoles = List.of(Rol.ADMINISTRADOR);
        return validateJwtWithBusinessRules(authHeader, allowedRoles, null);
    }

    public String validarGetCreador(String authHeader) {
        List<Rol> allowedRoles = List.of(Rol.ADMINISTRADOR, Rol.CREADOR);
        return validateJwtWithBusinessRules(authHeader, allowedRoles, null);
    }
    
    public Rol getRolFromToken(String authHeader) {
        try {
            String rolString = jwtUtil.getRolFromToken(authHeader);
            return Rol.valueOf(rolString);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autorización inválido");
        }
    }
    
    public boolean esAdmin(String authHeader) {
        String rolString = jwtUtil.getRolFromToken(authHeader);
        Rol userRole = Rol.valueOf(rolString);
        return userRole == Rol.ADMINISTRADOR;
    }
}