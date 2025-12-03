package com.esimedia.features.auth.services;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.dto.LoginRequestDTO;
import com.esimedia.features.auth.dto.LoginResponseDTO;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.shared.util.JwtUtil;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

@Service
public class AuthenticationService {

    @Value("${app.security.pepper}")
    private String pepper;

    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final Logger logger;

    // Constantes para evitar literales duplicados (SonarQube S1192)
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_UNKNOWN = "unknown";
    private static final String CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    private static final String CUENTA_BLOQUEADA_MSG = "La cuenta está bloqueada. Contacta con un administrador.";

    public AuthenticationService(UsuarioNormalRepository usuarioNormalRepository,
                                 CreadorContenidoRepository creadorContenidoRepository,
                                 AdminRepository adminRepository,
                                 JwtUtil jwtUtil,
                                 LoginAttemptService loginAttemptService) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
        this.logger = LoggerFactory.getLogger(AuthenticationService.class);
    }

    public LoginResponseDTO login(LoginRequestDTO credentials) {
        String email = credentials.getEmail();
        String password = credentials.getPassword();

        if (email == null || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y contraseña son campos obligatorios");
        }

    if (loginAttemptService.isBlocked(email, KEY_UNKNOWN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario temporalmente bloqueado");
        }

        Optional<UsuarioNormal> user = usuarioNormalRepository.findByemail(email);
        if (!user.isPresent() || !matchesPassword(password, user.get().getPassword())) {
            loginAttemptService.recordFailedAttempt(email, KEY_UNKNOWN);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        
        loginAttemptService.resetAttempts(email, KEY_UNKNOWN);
        String token = generateJwtToken(user.get());

        String aliasOrName;
        if (user.get().getAlias() != null && !user.get().getAlias().trim().isEmpty()) {
            aliasOrName = user.get().getAlias();
        }
		else {
            String nombre = user.get().getNombre() != null ? user.get().getNombre() : "";
            String apellidos = user.get().getApellidos() != null ? user.get().getApellidos() : "";
            String primerApellido = apellidos.split(" ")[0];
            aliasOrName = nombre + " " + primerApellido;
        }

        return new LoginResponseDTO(token, user.get().getEmail(), aliasOrName, user.get().getRol().name());
    }

    public Usuario authenticate(Map<String, String> credentials) {
        String email = credentials.get(KEY_EMAIL);
        String password = credentials.get(KEY_PASSWORD);

        if (email == null || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y contraseña son requeridos");
        }

        UsuarioNormal user = usuarioNormalRepository.findByemail(email).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }

        logger.info("Usuario {} confirmado: {}", email, user.isConfirmado());
        if (!user.isConfirmado()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta no ha sido confirmada. Por favor, revisa tu correo electrónico.");
        }

        if (user.isBloqueado()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, CUENTA_BLOQUEADA_MSG);
        }
    
        if (!matchesPassword(password, user.getPassword())) {
            logger.warn("Password incorrecta para usuario: {}", email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        logger.info("Autenticación exitosa para usuario: {}", email);
        return user;
    }

    

    public Usuario authenticatePrivilegedUser(Map<String, String> credentials) {
    String email = credentials.get(KEY_EMAIL);
    String password = credentials.get(KEY_PASSWORD);

    if (email == null || password == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Email y contraseña son requeridos"
        );
    }

    email = email.trim();

    logger.info("[LOGIN-PRIVILEGED] Intentando autenticación privilegiada para email: {}", email);

    Usuario authenticatedUser = null;

    // 1) Intentar primero como ADMINISTRADOR
    logger.info("[LOGIN-PRIVILEGED] Buscando usuario como ADMINISTRADOR para email: {}", email);
    Administrador admin = adminRepository.findByemail(email).orElse(null);
    if (admin != null) {
        logger.info("[LOGIN-PRIVILEGED] Usuario encontrado como ADMIN: {}", admin.getEmail());

        boolean passwordMatch = matchesPassword(password, admin.getPassword());
        logger.info("[LOGIN-PRIVILEGED] Comparando password para admin {}: {}", admin.getEmail(), passwordMatch);

        if (!passwordMatch) {
            logger.warn("[LOGIN-PRIVILEGED] Password incorrecta para admin {}", admin.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }

        authenticatedUser = admin;
    }

    if (authenticatedUser == null) {
        CreadorContenido creador = creadorContenidoRepository.findByemail(email).orElse(null);
        if (creador != null) {
            logger.info("[LOGIN-PRIVILEGED] Usuario encontrado como CREADOR: {} (validado: {}, bloqueado: {})",
                        creador.getEmail(), creador.isValidado(), creador.isBloqueado());

            if (!creador.isValidado()) {
                logger.warn("[LOGIN-PRIVILEGED] Creador no validado: {}", creador.getEmail());
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El creador de contenido no ha sido validado por un administrador"
                );
            }

            if (creador.isBloqueado()) {
                logger.warn("[LOGIN-PRIVILEGED] Creador bloqueado: {}", creador.getEmail());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, CUENTA_BLOQUEADA_MSG);
            }

            boolean passwordMatch = matchesPassword(password, creador.getPassword());
            logger.info("[LOGIN-PRIVILEGED] Comparando password para creador {}: {}", creador.getEmail(), passwordMatch);

            if (!passwordMatch) {
                logger.warn("[LOGIN-PRIVILEGED] Password incorrecta para creador {}", creador.getEmail());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
            }

            authenticatedUser = creador;
        }
    }
    if (authenticatedUser == null) {
        logger.warn("[LOGIN-PRIVILEGED] No se encontró usuario privilegiado para email: {}", email);
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
    }

    logger.info("[LOGIN-PRIVILEGED] Autenticación privilegiada correcta para email: {} con rol {}", 
                authenticatedUser.getEmail(), authenticatedUser.getRol());

    return authenticatedUser;
}


    public String generateJwtToken(Usuario user) {
        return jwtUtil.generateToken(user.getIdUsuario(), user.getEmail(), user.getCredentialsVersion(), user.getRol());
    }

    public String generateJwtToken(Usuario user, TipoContenido tipoContenido) {
        return jwtUtil.generateToken(user.getIdUsuario(), user.getEmail(), user.getCredentialsVersion(), user.getRol(), tipoContenido);
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        String passwordWithPepper = rawPassword + pepper;
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
             boolean result = argon2.verify(encodedPassword, passwordWithPepper.toCharArray());
            logger.info("Raw password: {}", rawPassword);
            logger.info("Password with pepper: {}", passwordWithPepper);
            
            logger.info("Stored (encoded) password: {}", encodedPassword);
            logger.info("Password match result: {}", result);
            return result;
        } 
        finally {
            argon2.wipeArray(passwordWithPepper.toCharArray());
        }
    }


    
}