package com.esimedia.features.user_management.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.services.SesionService;
import com.esimedia.features.user_management.dto.AdminProfileUpdateDTO;
import com.esimedia.features.user_management.dto.BlockUserDTO;
import com.esimedia.features.user_management.dto.CreatorProfileUpdateDTO;
import com.esimedia.features.user_management.dto.UserProfileUpdateDTO;
import com.esimedia.features.user_management.services.ManagementService;
import com.esimedia.features.user_management.services.PasswordManagementService;
import com.esimedia.features.user_management.services.UserQueryService;
import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.features.auth.dto.UserResponseDTO;
import com.esimedia.shared.util.JwtValidationUtil;




@RestController
@RequestMapping("/management")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class ManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ManagementController.class);

    // Constantes para strings duplicados
    private static final String USUARIO_NO_ENCONTRADO = "El usuario no ha podido ser encontrado";
    private static final String PERFIL_ACTUALIZADO_EXITOSAMENTE = "Perfil actualizado exitosamente";
    private static final String CONTRASENA_CAMBIADA_EXITOSAMENTE = "Contraseña cambiada exitosamente. Inicie sesión nuevamente.";
    private static final String NO_PERMISO_CAMBIAR_CONTRASENA = "No tienes permiso para cambiar esta contraseña";
    private static final String NO_PERMISO_ACTUALIZAR_PERFIL = "No tienes permiso para actualizar este perfil";
    private static final String NO_PERMISO_CAMBIAR_VIP = "No tienes permiso para cambiar el estado VIP";
    private static final String NO_PERMISO_CONSULTAR_VIP = "No tienes permiso para consultar el estado VIP";
    private static final String CREADOR_ELIMINADO_EXITOSAMENTE = "Creador eliminado exitosamente";
    private static final String ADMINISTRADOR_ELIMINADO_EXITOSAMENTE = "Administrador eliminado exitosamente";
    private static final String USUARIO_ELIMINADO_EXITOSAMENTE = "Usuario eliminado exitosamente";
    private static final String PERFIL_ACTUALIZADO_POR_ADMIN = "Perfil actualizado exitosamente por administrador";
    private static final String PERFIL_CREADOR_ACTUALIZADO_POR_ADMIN = "Perfil del creador actualizado exitosamente por administrador";
    private static final String PERFIL_ADMIN_ACTUALIZADO = "Perfil del administrador actualizado exitosamente";
    private static final String MESSAGE_KEY = "message";
    private static final String USER_ID_KEY = "userId";
    private static final String BLOCKED_KEY = "blocked";


    private final ManagementService managementService;
    private final SesionService sesionService;
    private final JwtValidationUtil jwtValidationService;
    private final UserRetrievalService userRetrievalService;
    private final PasswordManagementService passwordManagementService;
    private final UserQueryService userQueryService;

    public ManagementController(ManagementService managementService, 
                               SesionService sesionService, 
                               JwtValidationUtil jwtValidationService,
                               UserRetrievalService userRetrievalService,
                               PasswordManagementService passwordManagementService,
                               UserQueryService userQueryService) {
        this.managementService = managementService;
        this.sesionService = sesionService;
        this.jwtValidationService = jwtValidationService;
        this.userRetrievalService = userRetrievalService;
        this.passwordManagementService = passwordManagementService;
        this.userQueryService = userQueryService;
    }

    // ========== ENDPOINTS PARA GESTIÓN DE USUARIOS NORMALES ==========

    /**
     * Obtener perfil de usuario normal por ID
     */
    @GetMapping("/user/{userConsultId}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@RequestHeader("Authorization") String authHeader, @PathVariable String userConsultId) {
        try {
            
            String userId = jwtValidationService.validarGetUsuario(authHeader);

            UsuarioNormal user = userRetrievalService.findById(userConsultId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
            if (user.getIdUsuario().equals(userId) || jwtValidationService.esAdmin(authHeader)) {
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
                // Campos específicos de UsuarioNormal
                dto.setFlagVIP(user.isFlagVIP());
                dto.setFechaNacimiento(user.getFechaNacimiento());
                dto.setBloqueadoUsuarioNormal(user.isBloqueado());
                dto.setConfirmado(user.isConfirmado());
                return ResponseEntity.ok(dto);
            }
            else {
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, NO_PERMISO_CAMBIAR_CONTRASENA);
            }
        } 
        catch (RuntimeException e) {
            throw new IllegalStateException("Error obteniendo perfil de usuario ", e);
        }
    }

    /**
     * Obtener perfil de creador de contenido por ID
     * - El propio creador puede ver su perfil
     * - Los administradores pueden ver cualquier perfil de creador
     */
    @GetMapping("/creator/{creatorIdConsult}")
    public ResponseEntity<UserResponseDTO> getCreatorProfile(@RequestHeader("Authorization") String authHeader, @PathVariable String creatorIdConsult) {
        try {
            String authenticatedUserId = jwtValidationService.validarGetCreador(authHeader);

            CreadorContenido creator = managementService.findCreatorById(creatorIdConsult)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Creador de contenido no encontrado"));

            // El propio creador puede ver su perfil, o un administrador
            if (creator.getIdUsuario().equals(authenticatedUserId) || jwtValidationService.esAdmin(authHeader)) {
                UserResponseDTO dto = new UserResponseDTO();
                dto.setIdUsuario(creator.getIdUsuario());
                dto.setNombre(creator.getNombre());
                dto.setApellidos(creator.getApellidos());
                dto.setEmail(creator.getEmail());
                dto.setAlias(creator.getAlias());
                dto.setFotoPerfil(creator.getFotoPerfil());
                dto.setFechaRegistro(creator.getFechaRegistro());
                dto.setTwoFactorEnabled(creator.isTwoFactorEnabled());
                dto.setThirdFactorEnabled(creator.isThirdFactorEnabled());
                dto.setRol(creator.getRol());
                // Campos específicos de CreadorContenido
                dto.setAliasCreador(creator.getAliasCreador());
                dto.setDescripcion(creator.getDescripcion());
                dto.setBloqueadoCreador(creator.isBloqueado());
                dto.setEspecialidad(creator.getEspecialidad());
                dto.setTipoContenido(creator.getTipoContenido());
                dto.setValidado(creator.isValidado());
                return ResponseEntity.ok(dto);
            } 
            else {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, NO_PERMISO_CAMBIAR_CONTRASENA);
            }
        } 
        catch (RuntimeException e) {
            throw new IllegalStateException("Error obteniendo perfil de creador", e);
        }
    }

    /**
     * Obtener perfil de administrador por ID
     * - El propio administrador puede ver su perfil
     * - Los administradores pueden ver cualquier perfil de administrador
     */
    @GetMapping("/admin/{adminIdConsult}")
    public ResponseEntity<UserResponseDTO> getAdminProfile(@RequestHeader("Authorization") String authHeader, @PathVariable String adminIdConsult) {
        try {
            jwtValidationService.validarGetAdmin(authHeader);

            Administrador admin = managementService.findAdminById(adminIdConsult)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Administrador no encontrado"));
            
            UserResponseDTO dto = new UserResponseDTO();
            dto.setIdUsuario(admin.getIdUsuario());
            dto.setNombre(admin.getNombre());
            dto.setApellidos(admin.getApellidos());
            dto.setEmail(admin.getEmail());
            dto.setAlias(admin.getAlias());
            dto.setFotoPerfil(admin.getFotoPerfil());
            dto.setFechaRegistro(admin.getFechaRegistro());
            dto.setTwoFactorEnabled(admin.isTwoFactorEnabled());
            dto.setThirdFactorEnabled(admin.isThirdFactorEnabled());
            dto.setRol(admin.getRol());
            // Campos específicos de Administrador
            dto.setDepartamento(admin.getDepartamento());
            return ResponseEntity.ok(dto);

        } 
        catch (RuntimeException e) {
            throw new IllegalStateException("Error obteniendo perfil de administrador " + adminIdConsult, e);
        }
    }

    /**
     * Actualizar perfil de usuario normal
     * Campos permitidos: nombre, apellidos, alias, fechaNacimiento
     * NOTA: Los administradores NO pueden cambiar flagVIP ni contraseÃ±as
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<String> updateUserProfile(@RequestHeader("Authorization") String authHeader,
                                                   @PathVariable String userId,
                                                   @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        logger.info("Actualizando perfil de usuario: {}", userId);
        try {
            String authenticatedUserId = jwtValidationService.validarGenerico(authHeader);
            
            // Solo el propio usuario puede actualizar su perfil (no los administradores en este endpoint)
            if (!authenticatedUserId.equals(userId)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, NO_PERMISO_ACTUALIZAR_PERFIL);
            }

            managementService.updateNormalUserProfile(userId, updateDTO);
            return ResponseEntity.ok(PERFIL_ACTUALIZADO_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error actualizando perfil de usuario " + userId, e);
        }
    }


    /**
     * Cambiar contraseÃ±a de usuario (solo el propio usuario puede hacerlo)
     */
    @PutMapping("/user/{userId}/password")
    public ResponseEntity<String> changeUserPassword(@RequestHeader("Authorization") String authHeader,
                                                    @PathVariable String userId,
                                                    @Valid @RequestBody com.esimedia.features.user_management.dto.PasswordChangeDTO passwordChangeDTO) {
        logger.info("Cambio de contraseÃ±a solicitado para usuario: {}", userId);
        try {
            String authenticatedUserId = jwtValidationService.validarGetUsuario(authHeader);
            
            // Solo el propio usuario puede cambiar su contraseÃ±a
            if (!authenticatedUserId.equals(userId)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, NO_PERMISO_CAMBIAR_CONTRASENA);
            }

            passwordManagementService.changePassword(userId, passwordChangeDTO);

            // Expirar todas las sesiones para forzar re-login (rotación por cambio de credenciales)
            sesionService.eliminarTodasSesionesUsuario(userId);

            return ResponseEntity.ok(CONTRASENA_CAMBIADA_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error cambiando contraseÃ±a de usuario " + userId, e);
        }
    }

    /**
     * Cambiar contraseña de creador de contenido (solo el propio creador puede hacerlo)
     */
    @PutMapping("/creator/{creatorId}/password")
    public ResponseEntity<String> changeCreatorPassword(@RequestHeader("Authorization") String authHeader,
                                                       @PathVariable String creatorId,
                                                       @Valid @RequestBody com.esimedia.features.user_management.dto.PasswordChangeDTO passwordChangeDTO) {
        logger.info("Cambio de contraseÃ±a solicitado para creador: {}", creatorId);
        try {
            String authenticatedUserId = jwtValidationService.validarGetCreador(authHeader);
            
            // Solo el propio creador puede cambiar su contraseÃ±a
            if (!authenticatedUserId.equals(creatorId)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, NO_PERMISO_CAMBIAR_CONTRASENA);
            }

            passwordManagementService.changePassword(creatorId, passwordChangeDTO);

            // Expirar todas las sesiones para forzar re-login (rotaciÃ³n por cambio de credenciales)
            sesionService.eliminarTodasSesionesUsuario(creatorId);

            return ResponseEntity.ok(CONTRASENA_CAMBIADA_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error cambiando contraseÃ±a de creador " + creatorId, e);
        }
    }

    /**
     * Cambiar contraseÃ±a de administrador (solo el propio administrador puede hacerlo)
     */
    @PutMapping("/admin/{adminId}/password")
    public ResponseEntity<String> changeAdminPassword(@RequestHeader("Authorization") String authHeader,
                                                     @PathVariable String adminId,
                                                     @Valid @RequestBody com.esimedia.features.user_management.dto.PasswordChangeDTO passwordChangeDTO) {
        logger.info("Cambio de contraseÃ±a solicitado para administrador: {}", adminId);
        try {
            String authenticatedUserId = jwtValidationService.validarGetAdmin(authHeader);
            
            // Solo el propio administrador puede cambiar su contraseÃ±a
            if (!authenticatedUserId.equals(adminId)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, NO_PERMISO_CAMBIAR_CONTRASENA);
            }

            passwordManagementService.changePassword(adminId, passwordChangeDTO);

            // Expirar todas las sesiones para forzar re-login (rotación por cambio de credenciales)
            sesionService.eliminarTodasSesionesUsuario(adminId);

            return ResponseEntity.ok(CONTRASENA_CAMBIADA_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error cambiando contraseÃ±a de administrador " + adminId, e);
        }
    }

    // ========== ENDPOINTS PARA GESTIÃ“N DE ADMINISTRADORES ==========

    /**
     * Actualizar perfil de usuario como administrador
     * Campos permitidos: nombre, apellidos, alias, fechaNacimiento, flagVIP
     */
    @PutMapping("/admin/user/{userId}")
    public ResponseEntity<String> adminUpdateUserProfile(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable String userId,
                                                        @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        logger.info("Administrador actualizando perfil de usuario: {}", userId);
        try {
            jwtValidationService.validarGetAdmin(authHeader);
            managementService.adminUpdateUserProfile(userId, updateDTO);
            return ResponseEntity.ok(PERFIL_ACTUALIZADO_POR_ADMIN);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error en actualizaciÃ³n administrativa de usuario " + userId, e);
        }
    }

    /**
     * Bloquear/desbloquear usuario (solo para admins)
     */
    @PutMapping("/admin/user/{userId}/block")
    public ResponseEntity<Map<String, Object>> toggleUserBlock(@RequestHeader("Authorization") String authHeader,
                                                 @PathVariable String userId,
                                                 @Valid @RequestBody BlockUserDTO blockData) {
        logger.info("Cambio de estado de bloqueo para usuario: {}", userId);
        try {
            jwtValidationService.validarGetAdmin(authHeader);
           
            Boolean blocked = blockData.getBlocked();

            managementService.toggleUserBlock(userId, blocked);
            String message = Boolean.TRUE.equals(blocked) ? "Usuario bloqueado" : "Usuario desbloqueado";
            return ResponseEntity.ok(Map.of(
                MESSAGE_KEY, message,
                USER_ID_KEY, userId,
                BLOCKED_KEY, blocked
            ));
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error cambiando estado de bloqueo de usuario " + userId, e);
        }
    }

    /**
     * Bloquear/desbloquear creador de contenido (solo para admins)
     */
    @PutMapping("/admin/creator/{creatorId}/block")
    public ResponseEntity<Map<String, Object>> toggleCreatorBlock(@RequestHeader("Authorization") String authHeader,
                                                 @PathVariable String creatorId,
                                                 @Valid @RequestBody BlockUserDTO blockData) {
        logger.info("Cambio de estado de bloqueo para creador: {}", creatorId);
        try {
            jwtValidationService.validarGetAdmin(authHeader);
           
            Boolean blocked = blockData.getBlocked();

            managementService.toggleCreatorBlock(creatorId, blocked);
            String message = Boolean.TRUE.equals(blocked) ? "Creador bloqueado" : "Creador desbloqueado";
            return ResponseEntity.ok(Map.of(
                MESSAGE_KEY, message,
                "creatorId", creatorId,
                BLOCKED_KEY, blocked
            ));
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error cambiando estado de bloqueo de creador " + creatorId, e);
        }
    }

    // ========== ENDPOINTS PARA GESTIÃ“N DE PERFILES DE CREADORES Y ADMINS ==========

    /**
     * Actualizar perfil de creador de contenido
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil, aliasCreador, descripcion
     * NOTA: Los creadores NO pueden cambiar email ni tipo de contenido
     */
    @PutMapping("/creator/profile/{creatorId}")
    public ResponseEntity<String> updateCreatorProfile(@RequestHeader("Authorization") String authHeader,
                                                      @PathVariable String creatorId,
                                                      @Valid @RequestBody CreatorProfileUpdateDTO updateDTO) {
        logger.info("Actualizando perfil de creador: {}", creatorId);
        try {
            String authenticatedUserId = jwtValidationService.validarGetCreador(authHeader);
            
            // Solo el propio creador puede actualizar su perfil (no los administradores en este endpoint)
            if (!authenticatedUserId.equals(creatorId)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, NO_PERMISO_ACTUALIZAR_PERFIL);
            }

            managementService.updateCreatorProfile(creatorId, updateDTO);
            return ResponseEntity.ok(PERFIL_ACTUALIZADO_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error actualizando perfil de creador " + creatorId, e);
        }
    }

    /**
     * Actualizar perfil de administrador (cualquier administrador puede cambiar a cualquier otro)
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil
     * NOTA: Los administradores NO pueden cambiar email
     */
    @PutMapping("/admin/profile")
    public ResponseEntity<String> updateAdminProfile(@RequestHeader("Authorization") String authHeader,
                                                    @Valid @RequestBody AdminProfileUpdateDTO updateDTO) {
        logger.info("Actualizando perfil de administrador");
        try {
            String adminId = jwtValidationService.validarGetAdmin(authHeader);

            managementService.updateAdminProfile(adminId, updateDTO);
            return ResponseEntity.ok(PERFIL_ACTUALIZADO_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error actualizando perfil de administrador", e);
        }
    }

    /**
     * Actualizar perfil de creador de contenido como administrador
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil, aliasCreador, descripcion
     */
    @PutMapping("/admin/creator/{creatorId}")
    public ResponseEntity<String> adminUpdateCreatorProfile(@RequestHeader("Authorization") String authHeader,
                                                           @PathVariable String creatorId,
                                                           @Valid @RequestBody CreatorProfileUpdateDTO updateDTO) {
        logger.info("Administrador actualizando perfil de creador: {}", creatorId);
        try {
            jwtValidationService.validarGetAdmin(authHeader);
            managementService.adminUpdateCreatorProfile(creatorId, updateDTO);
            return ResponseEntity.ok(PERFIL_CREADOR_ACTUALIZADO_POR_ADMIN);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error en actualizaciÃ³n administrativa de creador " + creatorId, e);
        }
    }

    /**
     * Actualizar perfil de administrador como administrador (un admin editando otro admin)
     * Campos permitidos: nombre, apellidos, alias, fotoPerfil
     */
    @PutMapping("/admin/admin/{adminId}")
    public ResponseEntity<String> adminUpdateAdminProfile(@RequestHeader("Authorization") String authHeader,
                                                         @PathVariable String adminId,
                                                         @Valid @RequestBody AdminProfileUpdateDTO updateDTO) {
        logger.info("Administrador actualizando perfil de otro administrador: {}", adminId);
        try {
            jwtValidationService.validarGetAdmin(authHeader);
            managementService.adminUpdateAdminProfile(adminId, updateDTO);
            return ResponseEntity.ok(PERFIL_ADMIN_ACTUALIZADO);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error en actualizaciÃ³n administrativa de administrador " + adminId, e);
        }
    }

    // ========== ENDPOINTS PARA GESTIÃ“N DE CREADORES DE CONTENIDO ==========

    /**
     * Validar un creador de contenido pendiente
     */
    @PutMapping("/validate-creator/{creatorId}")
    public ResponseEntity<Map<String, String>> validateCreator(@RequestHeader("Authorization") String authHeader,
                                                 @PathVariable String creatorId) {
        logger.info("Validando creador con ID: {}", creatorId);
        try {
            // Solo administradores
            jwtValidationService.validarGetAdmin(authHeader); 
            String result = userQueryService.validateCreator(creatorId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, result));
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error validando creador " + creatorId, e);
        }
    }

    // ================================
    // ENDPOINTS DE HARD-DELETE (GDPR)
    // ================================

    /**
     * GDPR Hard-Delete: Admin elimina creador de contenido por ID
     * Solo administradores pueden usar este endpoint
     */
    @DeleteMapping("/admin/delete/creator/{creatorId}")
    public ResponseEntity<String> deleteCreatorAsAdmin(@RequestHeader("Authorization") String authHeader,
                                                      @PathVariable String creatorId) {
        logger.info("Admin eliminando creador: {}", creatorId);
        try {
            // Solo administradores
            jwtValidationService.validarGetAdmin(authHeader); 
            managementService.deleteCreatorAsAdmin(creatorId);
            return ResponseEntity.ok(CREADOR_ELIMINADO_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error eliminando creador " + creatorId, e);
        }
    }

    /**
     * GDPR Hard-Delete: Administrador elimina a otro administrador por ID
     * Solo administradores pueden usar este endpoint
     * Requiere que quede al menos un administrador en el sistema
     */
    @DeleteMapping("/admin/delete/admin/{adminId}")
    public ResponseEntity<String> deleteAdminAsAdmin(@RequestHeader("Authorization") String authHeader,
                                                      @PathVariable String adminId) {
        logger.info("Admin eliminando a otro administrador: {}", adminId);
        try {
            // Solo administradores
            String requestingAdminId = jwtValidationService.validarGetAdmin(authHeader);
            managementService.deleteAdminAsAdmin(adminId, requestingAdminId);
            return ResponseEntity.ok(ADMINISTRADOR_ELIMINADO_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error eliminando administrador " + adminId, e);
        }
    }

    /**
     * GDPR Hard-Delete: Creador se elimina a sÃ­ mismo
     * Solo creadores pueden usar este endpoint
     */
    @DeleteMapping("/creator/delete/self")
    public ResponseEntity<String> deleteCreatorSelf(@RequestHeader("Authorization") String authHeader) {
        logger.info("Creador auto-eliminaciÃ³n");
        try {
            String creatorId = jwtValidationService.validarGetCreador(authHeader);
            managementService.deleteCreatorSelf(creatorId);
            return ResponseEntity.ok(CREADOR_ELIMINADO_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error en auto-eliminaciÃ³n de creador", e);
        }
    }

    /**
     * GDPR Hard-Delete: Usuario normal se elimina a sÃ­ mismo
     * Solo usuarios normales pueden usar este endpoint
     */
    @DeleteMapping("/user/delete/self")
    public ResponseEntity<String> deleteUserSelf(@RequestHeader("Authorization") String authHeader) {
        logger.info("Usuario auto-eliminación");
        try {
            // Solo usuarios normales
            String userId = jwtValidationService.validarGenerico(authHeader); 
            managementService.deleteUserSelf(userId);
            return ResponseEntity.ok(USUARIO_ELIMINADO_EXITOSAMENTE);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error en auto-eliminación de usuario", e);
        }
    }

    // =============================
    // ENDPOINTS DE GESTIÃ“N VIP
    // =============================


    /**
     * Cambiar estado VIP del usuario autenticado
     * PUT /management/users/my-vip-status
     */
    @PutMapping("/users/my-vip-status")
    public ResponseEntity<String> changeMyVipStatus(@RequestHeader("Authorization") String authHeader,
                                                   @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        logger.info("Cambio de estado VIP solicitado");
        try {
            String userId = jwtValidationService.validarGetUsuario(authHeader);
            if (userId == null) {
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, NO_PERMISO_CAMBIAR_VIP);
            }
            managementService.changeUserVipStatus(userId, updateDTO.getFlagVIP());
            String statusText = Boolean.TRUE.equals(updateDTO.getFlagVIP()) ? "VIP" : "Normal";
            return ResponseEntity.ok("Estado cambiado a: " + statusText);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error cambiando estado VIP", e);
        }
    }


    /**
     * Obtener estado VIP del usuario autenticado
     * GET /management/users/my-vip-status
     */
    @GetMapping("/users/my-vip-status")
    public ResponseEntity<Map<String, Object>> getMyVipStatus(@RequestHeader("Authorization") String authHeader) {
        logger.info("Consultando estado VIP");
        try {
            String userId = jwtValidationService.validarGenerico(authHeader);
            if (userId == null) {
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, NO_PERMISO_CONSULTAR_VIP);
            }
            boolean isVip = managementService.getUserVipStatus(userId);
            Map<String, Object> response = Map.of(
                "flagVIP", isVip,
                "status", isVip ? "VIP" : "Normal"
            );
            return ResponseEntity.ok(response);
        }
        catch (RuntimeException e) {
            throw new IllegalStateException("Error obteniendo estado VIP", e);
        }
    }
}