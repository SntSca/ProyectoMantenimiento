package com.esimedia.features.user_management.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.SesionRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.services.ValoracionService;
import com.esimedia.features.favoritos.repository.ContenidoFavoritoRepository;
import com.esimedia.features.user_management.dto.AdminProfileUpdateDTO;
import com.esimedia.features.user_management.dto.CreatorProfileUpdateDTO;
import com.esimedia.features.user_management.dto.UserProfileUpdateDTO;

@Service
public class ManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ManagementService.class);
    
    // Constantes para evitar duplicación de literales
    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    private static final String CREADOR_NO_ENCONTRADO = "Creador no encontrado";
    private static final String ADMIN_NO_ENCONTRADO = "Administrador no encontrado";
    private static final String NOMBRE_FIELD = "nombre";
    private static final String APELLIDOS_FIELD = "apellidos";
    private static final String FOTO_PERFIL_BASE64_INVALIDO = "FotoPerfil no es un Base64 válido";
    private static final String DATA_URI_PREFIX = "data:";

    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository adminRepository;
    private final SesionRepository sesionRepository;
    private final ValidationService validationService;
    private final ValoracionService valoracionService;
    private final ContenidoFavoritoRepository contenidoFavoritoRepository;


    public ManagementService(UsuarioNormalRepository usuarioNormalRepository, CreadorContenidoRepository creadorContenidoRepository, AdminRepository adminRepository, SesionRepository sesionRepository, ValidationService validationService, ValoracionService valoracionService, ContenidoFavoritoRepository contenidoFavoritoRepository) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.adminRepository = adminRepository;
        this.sesionRepository = sesionRepository;
        this.validationService = validationService;
        this.valoracionService = valoracionService;
        this.contenidoFavoritoRepository = contenidoFavoritoRepository;
    }

    /**
     * Actualizar perfil de usuario normal (para uso propio del usuario)
     * Campos permitidos: nombre, apellidos, alias, fechaNacimiento
     * NO permite cambiar flagVIP (solo admins pueden hacerlo)
     */
    public void updateNormalUserProfile(String userId, UserProfileUpdateDTO updateDTO) {
        logger.info("[UPDATE-PROFILE] Actualizando perfil de usuario: {}", userId);

        UsuarioNormal user = getUserById(userId);
        
        updateBasicUserFields(user, updateDTO, userId);
        validateVipChangePermission(updateDTO.getFlagVIP(), false);
        
        usuarioNormalRepository.save(user);
        logger.info("[UPDATE-PROFILE] Perfil actualizado exitosamente para usuario: {}", userId);
    }

    /**
     * Método extraído para actualizar campos básicos de usuario (reduce complejidad ciclomática)
     */
    private void updateBasicUserFields(UsuarioNormal user, UserProfileUpdateDTO updateDTO, String userId) {
        updateFieldIfPresent(updateDTO.getNombre(), user::setNombre, NOMBRE_FIELD, ValidationService.MAX_NOMBRE_LENGTH);
        updateFieldIfPresent(updateDTO.getApellidos(), user::setApellidos, APELLIDOS_FIELD, ValidationService.MAX_APELLIDOS_LENGTH);
        
        // Solo validar unicidad del alias si el alias ha cambiado y no es null
        if (updateDTO.getAlias() != null && !updateDTO.getAlias().equals(user.getAlias())) {
            validateAliasUniquenessAcrossAllUsers(updateDTO.getAlias(), userId);
        }

        if (updateDTO.getAlias() != null) {
            user.setAlias(updateDTO.getAlias());
        }
    
        
        validationService.validateFechaNacimiento(updateDTO.getFechaNacimiento());
        user.setFechaNacimiento(updateDTO.getFechaNacimiento());

        // Procesar fotoPerfil
        String fotoPerfil = buildFotoPerfil(updateDTO.getFotoPerfil());
        if (fotoPerfil != null) {
            user.setFotoPerfil(fotoPerfil);
        }
    }

    /**
     * Método extraído para validar permisos de cambio VIP (reduce complejidad ciclomática)
     */
    private void validateVipChangePermission(Boolean flagVIP, boolean isAdmin) {
        if (flagVIP != null && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tienes permisos para cambiar el estado VIP");
        }
    }

    /**
     * Método extraído para actualizar campos con validación de longitud (reduce duplicación)
     */
    private void updateFieldIfPresent(String value, java.util.function.Consumer<String> setter, String fieldName, int maxLength) {
        if (value != null) {
            validationService.validateFieldLength(value, fieldName, maxLength);
            setter.accept(value);
        }
    }

    /**
     * Método para validar unicidad de alias entre todos los tipos de usuario
     */
    private void validateAliasUniquenessAcrossAllUsers(String alias, String currentUserId) {
        // Verificar contra usuarios normales
        Optional<UsuarioNormal> existingUser = usuarioNormalRepository.findByalias(alias);
        if (existingUser.isPresent() && !existingUser.get().getIdUsuario().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alias ya está en uso por un usuario");
        }

        // Verificar contra creadores de contenido
        Optional<CreadorContenido> existingCreator = creadorContenidoRepository.findByAlias(alias);
        if (existingCreator.isPresent() && !existingCreator.get().getIdUsuario().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alias ya está en uso por un creador");
        }

        // Verificar contra administradores
        Optional<Administrador> existingAdmin = adminRepository.findByalias(alias);
        if (existingAdmin.isPresent() && !existingAdmin.get().getIdUsuario().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alias ya está en uso por un administrador");
        }
    }

    /**
     * Actualizar perfil de usuario como administrador
     * Campos permitidos: nombre, apellidos, alias, fechaNacimiento
     * NOTA: Los administradores NO pueden cambiar contraseñas ni suscripción (flagVIP)
     */
    public void adminUpdateUserProfile(String userId, UserProfileUpdateDTO updateDTO) {
        logger.info("[ADMIN-UPDATE-PROFILE] Administrador actualizando perfil de usuario: {}", userId);

        UsuarioNormal user = getUserById(userId);
        
        updateBasicUserFields(user, updateDTO, userId);
        validateAdminVipPermission(updateDTO.getFlagVIP());
        
        usuarioNormalRepository.save(user);
        logger.info("[ADMIN-UPDATE-PROFILE] Perfil actualizado exitosamente por admin para usuario: {}", userId);
    }

    /**
     * Método extraído para validar permisos específicos de admin sobre VIP
     */
    private void validateAdminVipPermission(Boolean flagVIP) {
        if (flagVIP != null && flagVIP) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Los administradores no pueden modificar la suscripción de los usuarios");
        }
    }

    /**
     * Bloquear o desbloquear un usuario (solo para administradores)
     */
    public void toggleUserBlock(String userId, boolean blocked) {
        logger.info("[TOGGLE-BLOCK] {} usuario: {}", blocked ? "Bloqueando" : "Desbloqueando", userId);

        UsuarioNormal user = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        user.setBloqueado(blocked);
        usuarioNormalRepository.save(user);

        logger.info("[TOGGLE-BLOCK] Usuario {} {} exitosamente",
            userId, blocked ? "bloqueado" : "desbloqueado");
    }

    /**
     * Bloquear o desbloquear un creador de contenido (solo para administradores)
     */
    public void toggleCreatorBlock(String creatorId, boolean blocked) {
        logger.info("[TOGGLE-BLOCK] {} creador: {}", blocked ? "Bloqueando" : "Desbloqueando", creatorId);

        CreadorContenido creator = creadorContenidoRepository.findById(creatorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO));

        creator.setBloqueado(blocked);
        creadorContenidoRepository.save(creator);

        logger.info("[TOGGLE-BLOCK] Creador {} {} exitosamente",
            creatorId, blocked ? "bloqueado" : "desbloqueado");
    }

    /**
     * Obtener información de un usuario por ID
     */
    public UsuarioNormal getUserById(String userId) {
        return usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
    }

    /**
     * Verificar si un usuario existe
     */
    public boolean userExists(String userId) {
        return usuarioNormalRepository.existsById(userId);
    }

    /**
     * Actualizar perfil de creador de contenido (para uso propio del creador)
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil, aliasCreador, descripcion
     * NO permite cambiar email, tipoContenido, bloqueado, validado
     */
    public void updateCreatorProfile(String creatorId, CreatorProfileUpdateDTO updateDTO) {
        logger.info("[UPDATE-CREATOR-PROFILE] Actualizando perfil de creador: {}", creatorId);

        CreadorContenido creator = creadorContenidoRepository.findById(creatorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO));

        updateCreatorFields(creator, updateDTO, creatorId);

        creadorContenidoRepository.save(creator);
        logger.info("[UPDATE-CREATOR-PROFILE] Perfil actualizado exitosamente para creador: {}", creatorId);
    }

    /**
     * Método extraído para actualizar campos comunes de creador (reduce duplicación)
     */
    private void updateCreatorFields(CreadorContenido creator, CreatorProfileUpdateDTO updateDTO, String creatorId) {

        // Solo validar unicidad del alias si el alias ha cambiado y no es null
        if (updateDTO.getAlias() != null && !updateDTO.getAlias().equals(creator.getAlias())) {
            validateAliasUniquenessAcrossAllUsers(updateDTO.getAlias(), creatorId);
        }

        Optional<CreadorContenido> existingCreator = creadorContenidoRepository.findByAliasCreador(updateDTO.getAliasCreador());
        if (existingCreator.isPresent() && !existingCreator.get().getIdUsuario().equals(creatorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alias de creador ya está en uso");
        }

        creator.setNombre(updateDTO.getNombre());
        creator.setApellidos(updateDTO.getApellidos());
        creator.setAliasCreador(updateDTO.getAliasCreador());
        if (updateDTO.getAlias() != null) {
            creator.setAlias(updateDTO.getAlias());
        }

        creator.setDescripcion(updateDTO.getDescripcion());

        creator.setEspecialidad(updateDTO.getEspecialidad());

        // Procesar fotoPerfil
        String fotoPerfil = buildFotoPerfil(updateDTO.getFotoPerfil());
        if (fotoPerfil != null) {
            creator.setFotoPerfil(fotoPerfil);
        }
    }

    /**
     * Método extraído para actualizar campos comunes de administrador (reduce duplicación)
     */
    private void updateAdminFields(Administrador admin, AdminProfileUpdateDTO updateDTO, String adminId) {
        // Nombre - @NotBlank y @Size en DTO garantizan validaciones básicas
        admin.setNombre(updateDTO.getNombre());

        // Apellidos - @NotBlank y @Size en DTO garantizan validaciones básicas
        admin.setApellidos(updateDTO.getApellidos());

        // Alias - puede ser null, @Size y @Pattern en DTO si no es null
        if (updateDTO.getAlias() != null) {
            if (!updateDTO.getAlias().equals(admin.getAlias())) {
                validateAliasUniquenessAcrossAllUsers(updateDTO.getAlias(), adminId);
            }
            admin.setAlias(updateDTO.getAlias());
        }

        // Procesar fotoPerfil (ya viene como data URI completa)
        // fotoPerfil puede ser null, formatoFotoPerfil tiene @Pattern pero puede ser null
        if (updateDTO.getFotoPerfil() != null && !updateDTO.getFotoPerfil().trim().isEmpty()) {
            try {
                // La fotoPerfil ya viene como data URI completa
                admin.setFotoPerfil(updateDTO.getFotoPerfil());
            } 
            catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FOTO_PERFIL_BASE64_INVALIDO);
            }
        }
    }    
    /**
     * Actualizar perfil de administrador (para uso propio del administrador)
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil
     * NO permite cambiar email
     */
    public void updateAdminProfile(String adminId, AdminProfileUpdateDTO updateDTO) {
        logger.info("[UPDATE-ADMIN-PROFILE] Actualizando perfil de administrador: {}", adminId);

        Administrador admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ADMIN_NO_ENCONTRADO));

        updateAdminFields(admin, updateDTO, adminId);

        // Validación innecesaria - DTO ya garantiza que nombre y apellidos no son null

        adminRepository.save(admin);
        logger.info("[UPDATE-ADMIN-PROFILE] Perfil actualizado exitosamente para administrador: {}", adminId);
    }

    /**
     * Actualizar perfil de creador de contenido como administrador
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil, aliasCreador, descripcion, bloqueado, validado
     * NO permite cambiar email, tipoContenido, contraseña
     */
    public void adminUpdateCreatorProfile(String creatorId, CreatorProfileUpdateDTO updateDTO) {
        logger.info("[ADMIN-UPDATE-CREATOR-PROFILE] Administrador actualizando perfil de creador: {}", creatorId);

        CreadorContenido creator = creadorContenidoRepository.findById(creatorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO));

        updateCreatorFields(creator, updateDTO, creatorId);

        creadorContenidoRepository.save(creator);
        logger.info("[ADMIN-UPDATE-CREATOR-PROFILE] Perfil actualizado exitosamente por admin para creador: {}", creatorId);
    }

    /**
     * Actualizar perfil de administrador como administrador (un admin editando otro admin)
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil
     * NO permite cambiar email, contraseña
     */
    public void adminUpdateAdminProfile(String adminId, AdminProfileUpdateDTO updateDTO) {
        logger.info("[ADMIN-UPDATE-ADMIN-PROFILE] Administrador actualizando perfil de otro administrador: {}", adminId);

        Administrador admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ADMIN_NO_ENCONTRADO));

        updateAdminFields(admin, updateDTO, adminId);

        adminRepository.save(admin);
        logger.info("[ADMIN-UPDATE-ADMIN-PROFILE] Perfil actualizado exitosamente por admin para administrador: {}", adminId);
    }

    // ================================
    // MÉTODOS DE HARD-DELETE (GDPR)
    // ================================

   
    /**
     * GDPR Hard-Delete: Admin elimina creador de contenido específico por ID
     * Preserva contenido, elimina datos personales y sesiones
     */
    public void deleteCreatorAsAdmin(String creatorId) {
        logger.info("[HARD-DELETE-ADMIN] Eliminando creador por admin: {}", creatorId);
        
        CreadorContenido creador = creadorContenidoRepository.findById(creatorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO));
        
        // Eliminar sesiones del creador
        sesionRepository.deleteByIdUsuario(creatorId);
        
        // Eliminar creador (contenido se preserva por GDPR)
        creadorContenidoRepository.delete(creador);
        
        logger.info("[HARD-DELETE-ADMIN] Creador eliminado por admin: {}", creatorId);
    }

    /**
     * GDPR Hard-Delete: Administrador elimina a otro administrador
     * Preserva contenido, elimina datos personales y sesiones
     * Requiere que quede al menos un administrador en el sistema
     */
    public void deleteAdminAsAdmin(String adminIdToDelete, String requestingAdminId) {
        logger.info("[HARD-DELETE-ADMIN] Administrador {} eliminando a administrador {}", requestingAdminId, adminIdToDelete);
        
        // Verificar que no se esté intentando auto-eliminación
        if (requestingAdminId.equals(adminIdToDelete)) {
            throw new IllegalStateException("Los administradores no pueden eliminarse a sí mismos");
        }
        
        // Verificar que haya más de un administrador
        long adminCount = adminRepository.count();
        if (adminCount <= 1) {
            throw new IllegalStateException("No se puede eliminar el último administrador del sistema");
        }
        
        Administrador admin = adminRepository.findById(adminIdToDelete)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ADMIN_NO_ENCONTRADO));
        
        // Eliminar sesiones del administrador
        sesionRepository.deleteByIdUsuario(adminIdToDelete);
        
        // Eliminar administrador
        adminRepository.delete(admin);
        
        logger.info("[HARD-DELETE-ADMIN] Administrador eliminado exitosamente: {}", adminIdToDelete);
    }

    /**
     * GDPR Hard-Delete: Creador se elimina a sí mismo
     * Preserva contenido, elimina datos personales y sesiones
     */
    public void deleteCreatorSelf(String creatorId) {
        logger.info("[HARD-DELETE-SELF] Creador auto-eliminación: {}", creatorId);
        
        CreadorContenido creador = creadorContenidoRepository.findById(creatorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO));
        
        // Eliminar sesiones del creador
        sesionRepository.deleteByIdUsuario(creatorId);
        
        // Eliminar creador (el contenido se preserva para cumplir con GDPR)
        creadorContenidoRepository.delete(creador);
        
        logger.info("[HARD-DELETE-SELF] Creador auto-eliminado exitosamente: {}", creatorId);
    }

    /**
     * GDPR Hard-Delete: Usuario normal se elimina a sí mismo
     * Elimina completamente datos personales y sesiones
     */
    public void deleteUserSelf(String userId) {
        logger.info("[HARD-DELETE-SELF] Usuario auto-eliminación: {}", userId);
        
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
        
        // Eliminar valoraciones del usuario y actualizar valoraciones medias
        valoracionService.eliminarValoracionesDeUsuario(userId);
        
        // Eliminar sesiones del usuario
        sesionRepository.deleteByIdUsuario(userId);

        // Eliminar favoritos del usuario
        contenidoFavoritoRepository.deleteByIdUsuario(userId);
        
        // Eliminar usuario
        usuarioNormalRepository.delete(usuario);
        
        logger.info("[HARD-DELETE-SELF] Usuario auto-eliminado exitosamente: {}", userId);
    }

    // ================================
    // MÉTODOS DE GESTIÓN VIP
    // ================================

    /**
     * Cambiar estado VIP de un usuario (solo el propio usuario puede hacerlo)
     */
    public void changeUserVipStatus(String userId, boolean vipStatus) {
        logger.info("[VIP-STATUS-CHANGE] Cambiando estado VIP para usuario: {} a: {}", userId, vipStatus);
        
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
        
        usuario.setFlagVIP(vipStatus);
        usuarioNormalRepository.save(usuario);
        
        logger.info("[VIP-STATUS-CHANGE] Estado VIP cambiado exitosamente a {} para usuario: {}", vipStatus, userId);
    }

    /**
     * Obtener estado VIP actual de un usuario
     */
    public boolean getUserVipStatus(String userId) {
        logger.info("[VIP-STATUS-GET] Obteniendo estado VIP para usuario: {}", userId);
        
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
        
        return usuario.isFlagVIP();
    }

    /**
     * Obtener perfil de creador de contenido por ID
     */
    public Optional<CreadorContenido> findCreatorById(String id) {
        return creadorContenidoRepository.findById(id);
    }

    /**
     * Obtener perfil de administrador por ID
     */
    public Optional<Administrador> findAdminById(String id) {
        return adminRepository.findById(id);
    }

    private String buildFotoPerfil(String fotoPerfil) {
        if (fotoPerfil == null || fotoPerfil.trim().isEmpty()) {
            return null;
        }
        if (fotoPerfil.startsWith(DATA_URI_PREFIX)) {
            return fotoPerfil;
        }
        return null;
    }
}
