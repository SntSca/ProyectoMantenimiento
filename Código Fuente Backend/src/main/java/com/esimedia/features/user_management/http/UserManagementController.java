package com.esimedia.features.user_management.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.user_management.services.UserListService;
import com.esimedia.features.user_management.services.UserQueryService;
import com.esimedia.shared.util.JwtValidationUtil;

@RestController
@RequestMapping("/users/manage")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    // Constantes para literales utilizados frecuentemente
    private static final String INVALID_TOKEN_MSG = "Token de autorización inválido";

    private final UserQueryService userQueryService;
    private final UserListService userListService;
    private final JwtValidationUtil jwtValidationService;

    public UserManagementController(UserQueryService userQueryService,
                                   UserListService userListService,
                                   JwtValidationUtil jwtValidationService) {
        this.userQueryService = userQueryService;
        this.userListService = userListService;
        this.jwtValidationService = jwtValidationService;
    }

    @PutMapping("/validate-creator/{creatorId}")
    public String validateCreator(@RequestHeader("Authorization") String authHeader, @PathVariable String creatorId) {
        jwtValidationService.validarGetAdmin(authHeader);
        return this.userQueryService.validateCreator(creatorId);
    }

    /**
     * Obtiene todos los usuarios de todos los tipos en una sola respuesta
     * Requiere autenticación JWT válida
     */
    @GetMapping("/all-users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = jwtValidationService.validarGetAdmin(authHeader);
            logger.info("Usuario {} solicitando lista de todos los usuarios", userId);

            Map<String, Object> allUsers = userListService.getAllUsers();
            logger.info("Se encontraron todos los usuarios para usuario {}", userId);
            return ResponseEntity.ok(allUsers);
        } 
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG, e);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
            "Error interno al obtener todos los usuarios", e);
        }
    }
}