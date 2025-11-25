package com.esimedia.features.auth.http;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.services.AuthenticationService;
import com.esimedia.features.auth.services.LoginAttemptService;
import com.esimedia.features.auth.services.SesionService;
import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.features.auth.services.SessionTimeoutService;
import com.esimedia.shared.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users/auth")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    // Constantes para literales utilizados frecuentemente
    private static final String EMAIL_FIELD = "email";
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String SEGUNDOS_LITERAL = "segundos";
    private static final String TOKEN_FIELD = "token";
    private static final String FROM_IP_LITERAL = " desde IP: ";
    private static final String DEMASIADOS_INTENTOS_USUARIO = "Demasiados intentos para este usuario. Inténtalo de nuevo en ";
    private static final String DEMASIADOS_INTENTOS_LOGIN_IP = "Demasiados intentos desde esta IP. Inténtalo de nuevo en ";
    private static final String ERROR_AUTENTICACION_PREFIX = "Error de autenticación para usuario: ";
    private static final String ERROR_INTERNO_LOGOUT = "Error interno durante el logout";
    private static final String LOGOUT_EXITOSO = "Logout exitoso";
    private static final String LOGOUT_PROCESADO = "Logout procesado (no había sesión activa)";
    private static final String MESSAGE_KEY = "message";
    private static final String ERROR_KEY = "error";
    private static final String TWO_FACTOR_ENABLED_KEY = "twoFactorEnabled";
    private static final String THIRD_FACTOR_ENABLED_KEY = "thirdFactorEnabled";


    private final AuthenticationService authenticationService;
    private final SesionService sesionService;
    private final LoginAttemptService attemptService;
    private final UserRetrievalService userRetrievalService;
    private final SessionTimeoutService sessionTimeoutService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationService authenticationService,
                         SesionService sesionService,
                         LoginAttemptService attemptService,
                         UserRetrievalService userRetrievalService,
                         SessionTimeoutService sessionTimeoutService,
                         JwtUtil jwtUtil) {
        this.authenticationService = authenticationService;
        this.sesionService = sesionService;
        this.attemptService = attemptService;
        this.userRetrievalService = userRetrievalService;
        this.sessionTimeoutService = sessionTimeoutService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String username = credentials.getOrDefault(EMAIL_FIELD, "");
        logger.info("[LOGIN] Petición recibida en /users/auth/login para usuario: {} desde IP: {}", username, ip);

        if (attemptService.isIpBlocked(ip)) {
            long retryAfter = attemptService.getIpRetryAfterSeconds(ip);
            logger.warn("[LOGIN] IP bloqueada: {}. Retry-After: {}", ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body(DEMASIADOS_INTENTOS_LOGIN_IP + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }
        if (attemptService.isBlocked(username, ip)) {
            long retryAfter = attemptService.getUserRetryAfterSeconds(username, ip);
            logger.warn("[LOGIN] Usuario bloqueado: {} desde IP: {}. Retry-After: {}", username, ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body(DEMASIADOS_INTENTOS_USUARIO + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }

        try {
            // Limpiar sesiones existentes del usuario antes de autenticar
            Optional<Usuario> existingUser = this.userRetrievalService.findAnyUserByEmail(username);
            if (existingUser.isPresent()) {
                logger.info("[LOGIN] Limpiando sesiones existentes para usuario: {}", username);
                sesionService.eliminarTodasSesionesUsuario(existingUser.get().getIdUsuario());
                sesionService.limpiarTodasLasSesiones(existingUser.get().getIdUsuario());
            }

            logger.info("[LOGIN] Llamando a service.authenticate para usuario: {}", username);
            Usuario user = this.authenticationService.authenticate(credentials);
            logger.info("[LOGIN] Autenticación exitosa para usuario: {}", username);
            
            
            // Si no tiene 2FA/3FA, proceder con login tradicional
            attemptService.resetAttempts(username, ip);
            String jwt = this.authenticationService.generateJwtToken(user);
            logger.info("[LOGIN] Token generado para usuario: {}", username);
            
            // Rotar sesión por fixation (expira anteriores y crea nueva)
            sesionService.rotarSesionPorFixation(String.valueOf(user.getIdUsuario()), ip, jwt);
            logger.info("[LOGIN] Sesión rotada para usuario: {}", username);
            
            // Registrar sesión con absolute timeout
            sessionTimeoutService.registerSession(String.valueOf(user.getIdUsuario()), user.getRol());
            logger.info("[LOGIN] Absolute timeout registrado para usuario {} con rol {}", username, user.getRol());
            
            return ResponseEntity.ok(Map.of(
                TOKEN_FIELD, jwt,
                TWO_FACTOR_ENABLED_KEY, user.isTwoFactorEnabled(),
                THIRD_FACTOR_ENABLED_KEY, user.isThirdFactorEnabled()
            ));
        }
        catch (ResponseStatusException e) {
            // Si es FORBIDDEN (cuenta bloqueada), relanzar tal cual para preservar el mensaje
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw e;
            }
            attemptService.recordFailedAttempt(username, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                ERROR_AUTENTICACION_PREFIX + username + FROM_IP_LITERAL + ip, e);
        }
        catch (Exception e) {
            attemptService.recordFailedAttempt(username, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                ERROR_AUTENTICACION_PREFIX + username + FROM_IP_LITERAL + ip, e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader("Authorization") String authHeader) {
        logger.info("[LOGOUT] Petición de logout recibida");
        try{
            // El token ya fue validado por JwtFilter, no necesitamos validar de nuevo
            String userId = jwtUtil.getUserIdFromToken(authHeader);
            authHeader = authHeader.substring(7);
            
            //Buscar el usuario con el id recibido
            boolean sesionEliminada = sesionService.eliminarSesionPorJwt(authHeader);
            
            // Invalidar también la sesión del timeout absoluto
            sessionTimeoutService.invalidateSession(userId);
            logger.info("[LOGOUT] Sesión de timeout absoluto invalidada para usuario: {}", userId);

            if (sesionEliminada) {
                logger.info("[LOGOUT] Sesión eliminada exitosamente");
                return ResponseEntity.ok(Map.of(MESSAGE_KEY, LOGOUT_EXITOSO));
            }
            else {
                logger.warn("[LOGOUT] No se encontró sesión para el JWT proporcionado");
                return ResponseEntity.ok(Map.of(MESSAGE_KEY, LOGOUT_PROCESADO));
            }
            
        }
        catch (Exception e) {
            logger.error("[LOGOUT] Error durante el logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(ERROR_KEY, ERROR_INTERNO_LOGOUT));
        }
    }

    @PostMapping("/privileged-login")
    public ResponseEntity<Object> privilegedLogin(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String username = credentials.getOrDefault(EMAIL_FIELD, "");

        if (attemptService.isIpBlocked(ip)) {
            long retryAfter = attemptService.getIpRetryAfterSeconds(ip);
            logger.warn("[LOGIN] IP bloqueada: {}. Retry-After: {}", ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body(DEMASIADOS_INTENTOS_LOGIN_IP + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }
        if (attemptService.isBlocked(username, ip)) {
            long retryAfter = attemptService.getUserRetryAfterSeconds(username, ip);
            logger.warn("[LOGIN] Usuario bloqueado: {} desde IP: {}. Retry-After: {}", username, ip, retryAfter);
            return ResponseEntity.status(429)
                .header(RETRY_AFTER_HEADER, String.valueOf(retryAfter))
                .body(DEMASIADOS_INTENTOS_USUARIO + retryAfter + " " + SEGUNDOS_LITERAL + ".");
        }

        try {
            // Limpiar sesiones existentes del usuario antes de autenticar
            Optional<Usuario> existingUser = this.userRetrievalService.findAnyUserByEmail(username);
            if (existingUser.isPresent()) {
                logger.info("[LOGIN] Limpiando sesiones existentes para usuario privilegiado: {}", username);
                sesionService.eliminarTodasSesionesUsuario(existingUser.get().getIdUsuario());
                sesionService.limpiarSesionesExpiradasEInvalidas();
                // Eliminar tras la revisión
                sesionService.limpiarTodasLasSesiones(existingUser.get().getIdUsuario());
            }

            logger.info("[LOGIN-PRIVILEGED] Autenticando usuario privilegiado: {}", username);
            Usuario user = this.authenticationService.authenticatePrivilegedUser(credentials);
            logger.info("[LOGIN-PRIVILEGED] Autenticación exitosa para usuario privilegiado: {}", username);
            
            
            // Si no tiene 2FA/3FA, proceder con login tradicional
            attemptService.resetAttempts(username, ip);
            String jwt;
            if (user instanceof CreadorContenido creador) {
                jwt = this.authenticationService.generateJwtToken(creador, creador.getTipoContenido());
            }
            else {
                jwt = this.authenticationService.generateJwtToken(user);
            }
            logger.info("[LOGIN-PRIVILEGED] Token generado para usuario: {}", username);
            
            // Rotar sesión por fixation (expira anteriores y crea nueva)
            sesionService.rotarSesionPorFixation(String.valueOf(user.getIdUsuario()), ip, jwt);
            
            // Registrar sesión con absolute timeout
            sessionTimeoutService.registerSession(String.valueOf(user.getIdUsuario()), user.getRol());
            logger.info("[LOGIN-PRIVILEGED] Absolute timeout registrado para usuario {} con rol {}", username, user.getRol());
            
            return ResponseEntity.ok(Map.of(
                TOKEN_FIELD, jwt,
                TWO_FACTOR_ENABLED_KEY, user.isTwoFactorEnabled(),
                THIRD_FACTOR_ENABLED_KEY, user.isThirdFactorEnabled()
            ));
        }
        catch (ResponseStatusException e) {
            // Si es FORBIDDEN (cuenta bloqueada), relanzar tal cual para preservar el mensaje
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw e;
            }
            attemptService.recordFailedAttempt(username, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                ERROR_AUTENTICACION_PREFIX + username + FROM_IP_LITERAL + ip, e);
        }
        catch (Exception e) {
            attemptService.recordFailedAttempt(username, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                ERROR_AUTENTICACION_PREFIX + username + FROM_IP_LITERAL + ip, e);
        }
    }

    @PostMapping("/logout/session")
    public ResponseEntity<Object> logoutSession(@RequestBody Map<String, String> requestBody) {
        logger.info("[LOGOUT SESSION] Petición de logout de sesión específica recibida");
        ResponseEntity<Object> response;
        try {
            String jwt = requestBody.get("jwt");
            if (jwt == null || jwt.trim().isEmpty()) {
                logger.warn("[LOGOUT SESSION] JWT no proporcionado");
                response = ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "JWT requerido"));
            }
            else {
                // Eliminar la sesión de la base de datos por JWT
                boolean sesionEliminada = sesionService.eliminarSesionPorJwt(jwt);

                if (sesionEliminada) {
                    logger.info("[LOGOUT SESSION] Sesión eliminada exitosamente para JWT");
                    response = ResponseEntity.ok(Map.of(MESSAGE_KEY, LOGOUT_EXITOSO));
                }
                else {
                    logger.warn("[LOGOUT SESSION] No se encontró sesión para el JWT proporcionado");
                    response = ResponseEntity.ok(Map.of(MESSAGE_KEY, LOGOUT_PROCESADO));
                }
            }
        }
        catch (Exception e) {
            logger.error("[LOGOUT SESSION] Error durante el logout de sesión", e);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(ERROR_KEY, ERROR_INTERNO_LOGOUT));
        }
        return response;
    }
}