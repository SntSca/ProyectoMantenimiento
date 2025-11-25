package com.esimedia.features.user_management.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.esimedia.features.auth.dto.UserResponseDTO;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

/**
 * Servicio especializado para operaciones de listado masivo de usuarios.
 * Se encarga de obtener listas completas de usuarios por tipo o todos juntos.
 */
@Service
public class UserListService {

    private static final String ERROR_OBTENER_USUARIOS_NORMALES = "Error al obtener usuarios normales";
    private static final String ERROR_OBTENER_ADMINISTRADORES = "Error al obtener administradores";
    private static final String ERROR_OBTENER_CREADORES_CONTENIDO = "Error al obtener creadores de contenido";
    private static final String ERROR_OBTENER_TODOS_USUARIOS = "Error al obtener todos los usuarios";
    
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository adminRepository;
    private final Logger logger;

    public UserListService(UsuarioNormalRepository usuarioNormalRepository,
                          CreadorContenidoRepository creadorContenidoRepository,
                          AdminRepository adminRepository) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.adminRepository = adminRepository;
        this.logger = LoggerFactory.getLogger(UserListService.class);
    }

    /**
     * Obtiene todos los usuarios normales de la base de datos
     */
    public List<UsuarioNormal> getAllUsuariosNormales() {
        logger.info("[GET_ALL_NORMAL_USERS] Obteniendo todos los usuarios normales");
        try {
            List<UsuarioNormal> usuarios = usuarioNormalRepository.findAll();
            logger.info("[GET_ALL_NORMAL_USERS] Se encontraron {} usuarios normales", usuarios.size());
            return usuarios;
        } 
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_OBTENER_USUARIOS_NORMALES, e);
        }
    }

    /**
     * Obtiene todos los administradores de la base de datos
     */
    public List<Administrador> getAllAdministradores() {
        logger.info("[GET_ALL_ADMINISTRATORS] Obteniendo todos los administradores");
        try {
            List<Administrador> administradores = adminRepository.findAll();
            logger.info("[GET_ALL_ADMINISTRATORS] Se encontraron {} administradores", administradores.size());
            return administradores;
        } 
		catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_OBTENER_ADMINISTRADORES, e);
        }
    }

    /**
     * Obtiene todos los creadores de contenido de la base de datos
     */
    public List<CreadorContenido> getAllCreadoresContenido() {
        logger.info("[GET_ALL_CONTENT_CREATORS] Obteniendo todos los creadores de contenido");
        try {
            List<CreadorContenido> creadores = creadorContenidoRepository.findAll();
            logger.info("[GET_ALL_CONTENT_CREATORS] Se encontraron {} creadores de contenido", creadores.size());
            return creadores;
        } 
		catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_OBTENER_CREADORES_CONTENIDO, e);
        }
    }

    /**
     * Convierte un Usuario a UserResponseDTO con fotoPerfil como data URI
     */
    private UserResponseDTO convertToDTO(Usuario user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setIdUsuario(user.getIdUsuario());
        dto.setNombre(user.getNombre());
        dto.setApellidos(user.getApellidos());
        dto.setEmail(user.getEmail());
        dto.setAlias(user.getAlias());
        dto.setFotoPerfil(user.getFotoPerfil());
        dto.setFechaRegistro(user.getFechaRegistro());
        dto.setTwoFactorEnabled(user.isTwoFactorEnabled());
        dto.setThirdFactorEnabled(user.isThirdFactorEnabled());
        dto.setRol(user.getRol());

        // Campos específicos según el tipo
        if (user instanceof UsuarioNormal normal) {
            dto.setFlagVIP(normal.isFlagVIP());
            dto.setFechaNacimiento(normal.getFechaNacimiento());
            dto.setBloqueadoUsuarioNormal(normal.isBloqueado());
            dto.setConfirmado(normal.isConfirmado());
        }
        else if (user instanceof CreadorContenido creador) {
            dto.setAliasCreador(creador.getAliasCreador());
            dto.setDescripcion(creador.getDescripcion());
            dto.setBloqueadoCreador(creador.isBloqueado());
            dto.setEspecialidad(creador.getEspecialidad());
            dto.setTipoContenido(creador.getTipoContenido());
            dto.setValidado(creador.isValidado());
        }
        else if (user instanceof Administrador admin) {
            dto.setDepartamento(admin.getDepartamento());
        }

        return dto;
    }

    /**
     * Obtiene todos los usuarios (normales, administradores y creadores) en una sola lista
     * @return Map con las listas de cada tipo de usuario, con fotos convertidas a data URIs
     */
    public Map<String, Object> getAllUsers() {
        logger.info("[GET_ALL_USERS] Obteniendo todos los usuarios de todos los tipos");
        try {
            List<UsuarioNormal> usuariosNormales = usuarioNormalRepository.findAll();
            List<Administrador> administradores = adminRepository.findAll();
            List<CreadorContenido> creadores = creadorContenidoRepository.findAll();
            
            // Convertir a DTOs con fotos como data URIs
            List<UserResponseDTO> normalesDTO = usuariosNormales.stream()
                .map(this::convertToDTO)
                .toList();
            List<UserResponseDTO> adminsDTO = administradores.stream()
                .map(this::convertToDTO)
                .toList();
            List<UserResponseDTO> creadoresDTO = creadores.stream()
                .map(this::convertToDTO)
                .toList();
            
            logger.info("[GET_ALL_USERS] Se encontraron {} usuarios normales, {} administradores, {} creadores", 
                usuariosNormales.size(), administradores.size(), creadores.size());
            
            return Map.of(
                "normalUsers", normalesDTO,
                "administrators", adminsDTO,
                "contentCreators", creadoresDTO
            );
        } 
		catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_OBTENER_TODOS_USUARIOS, e);
        }
    }
}