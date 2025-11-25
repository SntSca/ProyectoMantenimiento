package com.esimedia.features.user_registration.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import com.esimedia.features.auth.dto.AdministradorDTO;
import com.esimedia.features.auth.dto.CreadorContenidoDTO;
import com.esimedia.features.auth.dto.UsuarioNormalDTO;
import com.esimedia.features.auth.services.LoginAttemptService;
import com.esimedia.features.user_registration.services.UserRegistrationService;
import com.esimedia.shared.util.JwtValidationUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users/register")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    
    // Constantes para literales utilizados frecuentemente
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String SEGUNDOS_LITERAL = "segundos";
    private static final String DEMASIADOS_INTENTOS_IP = "Demasiados intentos de registro desde esta IP. Inténtalo de nuevo en ";
    private static final String ERROR_REGISTRO_PREFIX = "Error de registro: ";
    private static final String ERROR_INTERNO_REGISTRO = "Error interno durante el registro del administrador";

    private final UserRegistrationService userRegistrationService;
    private final JwtValidationUtil jwtValidationService;
    private final LoginAttemptService attemptService;

    public RegistrationController(UserRegistrationService userRegistrationService,
                                 JwtValidationUtil jwtValidationService,
                                 LoginAttemptService attemptService) {
        this.userRegistrationService = userRegistrationService;
        this.jwtValidationService = jwtValidationService;
        this.attemptService = attemptService;
    }

    @PostMapping("/standard")
    public ResponseEntity<Object> registerStandard(@Valid @RequestBody UsuarioNormalDTO usuarioNormalDTO) {
        logger.info("Llamada recibida: {}", usuarioNormalDTO);
        String result = this.userRegistrationService.registerNormalUser(usuarioNormalDTO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/creator")
    public ResponseEntity<Object> registerCreator(@Valid @RequestBody CreadorContenidoDTO creadorDTO) {
        logger.info("Registro de creador recibido: {}", creadorDTO);
        String result = this.userRegistrationService.registerCreator(creadorDTO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/admin")
    public ResponseEntity<Object> registerAdmin(@Valid @RequestBody AdministradorDTO adminDTO, 
    @RequestHeader("Authorization") String authHeader,
    HttpServletRequest request)  {

        // Valida que sea un administrador el que lo está realizando
        jwtValidationService.validarGetAdmin(authHeader);

        String ip = request.getRemoteAddr();
        logger.info("Registro de administrador recibido: {} desde IP: {}", adminDTO.getEmail(), ip);
        
        ResponseEntity<Object> response;

        // Rate limiting por IP para registro de administradores (más restrictivo)
        if (attemptService.isIpBlocked(ip)) {
            long retryAfter = attemptService.getIpRetryAfterSeconds(ip);
            logger.warn("[REGISTER-ADMIN] IP bloqueada: {}. Retry-After: {}", ip, retryAfter);
            response = ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body(DEMASIADOS_INTENTOS_IP + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }
        else {
            try {
                String result = this.userRegistrationService.registerAdmin(adminDTO);
                logger.info("[REGISTER-ADMIN] Administrador registrado exitosamente: {} - Departamento: {}", 
                    adminDTO.getEmail(), adminDTO.getDepartamento());
                response = ResponseEntity.ok(result);
            }
            catch (ResponseStatusException e) {
                attemptService.recordFailedAttempt("admin-reg-" + ip, ip);
                logger.warn("[REGISTER-ADMIN] Error de validación para administrador {}: {}", 
                    adminDTO.getEmail(), e.getReason());
                response = ResponseEntity.status(e.getStatusCode()).body(ERROR_REGISTRO_PREFIX + e.getReason());
            }
            catch (Exception e) {
                attemptService.recordFailedAttempt("admin-reg-" + ip, ip);
                logger.error("[REGISTER-ADMIN] Error interno en registro de administrador {}: {}", 
                    adminDTO.getEmail(), e.getMessage(), e);
                response = ResponseEntity.status(500).body(ERROR_INTERNO_REGISTRO);
            }
        }

        return response;
    }

    // Endpoint para confirmar la cuenta de un usuario mediante un token
    @GetMapping("/confirm/{tokenId}")
    public ResponseEntity<String> confirmAccount(@PathVariable("tokenId") String tokenId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        // Rate limiting por IP para confirmaciones
        if (attemptService.isIpBlocked(ip)) {
            long retryAfter = attemptService.getIpRetryAfterSeconds(ip);
            logger.warn("[CONFIRM] IP bloqueada para confirmación: {}. Retry-After: {}", ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body("Demasiados intentos de confirmación desde esta IP. Inténtalo de nuevo en " + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }

        try {
            String result = userRegistrationService.confirmUserAccount(tokenId);
            return ResponseEntity.ok(result);
        }
        catch (ResponseStatusException e) {
            // Registrar intento fallido de confirmación por IP
            attemptService.recordFailedAttempt("confirmation-" + ip, ip);
            logger.error("[CONFIRM-ACCOUNT] Error en confirmación desde IP {}: {}", ip, e.getReason(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}