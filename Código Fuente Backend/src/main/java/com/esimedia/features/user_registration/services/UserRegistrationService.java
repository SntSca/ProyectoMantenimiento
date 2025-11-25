package com.esimedia.features.user_registration.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoToken;
import com.esimedia.features.auth.dto.AdministradorDTO;
import com.esimedia.features.auth.dto.CreadorContenidoDTO;
import com.esimedia.features.auth.dto.UsuarioNormalDTO;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Token;
import com.esimedia.features.auth.entity.UsuarioFactory;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.services.EmailService;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.TokenRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

@Service
public class UserRegistrationService {

    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository adminRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final ValidationService validationService;
    private final UsuarioFactory usuarioFactory;
    private final Logger logger;

    public UserRegistrationService(UsuarioNormalRepository usuarioNormalRepository,
                                   CreadorContenidoRepository creadorContenidoRepository,
                                   AdminRepository adminRepository,
                                   TokenRepository tokenRepository,
                                   EmailService emailService,
                                   ValidationService validationService,
                                   UsuarioFactory usuarioFactory) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.adminRepository = adminRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.validationService = validationService;
        this.usuarioFactory = usuarioFactory;
        this.logger = LoggerFactory.getLogger(UserRegistrationService.class);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     */
    public String registerNormalUser(UsuarioNormalDTO usuarioDTO) throws ResponseStatusException {
        logger.info("[REGISTER] Iniciando registro de usuario normal: {}", usuarioDTO.getEmail());
        UsuarioNormal usuario = null;
        String result = null;
        try {

            logger.info("[REGISTER] Iniciando validaciones para: {}", usuarioDTO.getEmail());
            validateNormalUserInput(
                usuarioDTO.getEmail(),
                usuarioDTO.getAlias(),
                usuarioDTO.getPassword(),
                usuarioDTO.getFechaNacimiento()
            );
            logger.info("[REGISTER] Creando usuario con UsuarioFactory");
            usuario = (UsuarioNormal) usuarioFactory.crearUsuario(usuarioDTO, validationService);
            logger.info("[REGISTER] Usuario creado: {}", usuario.getEmail());
            usuario.setConfirmado(false);
            logger.info("[REGISTER] Usuario marcado como no confirmado");
            logger.info("[REGISTER] Guardando usuario en base de datos");
            usuarioNormalRepository.save(usuario);
            logger.info("[REGISTER] Usuario guardado exitosamente con ID: {}", usuario.getIdUsuario());
            String confirmationToken = generateConfirmationToken();
            Token token = Token.builder()
                .id(usuario.getIdUsuario())
                .tokenCreado(confirmationToken)
                .tipoToken(TipoToken.CONFIRMACION_CUENTA)
                .fechaInicio(LocalDateTime.now())
                .fechaUltimaActividad(LocalDateTime.now().plusDays(1))
                .estado(EstadoToken.SIN_CONFIRMAR)
                .usuarioEmail(usuario.getEmail())
                .build();
            logger.info("[REGISTER] Token objeto creado, guardando en BD");
            tokenRepository.save(token);
            logger.info("[REGISTER] Token guardado exitosamente");
            logger.info("[REGISTER] Enviando email de confirmación");
            String expirationTime = token.getFechaInicio().plusHours(24).format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));
            emailService.sendConfirmationEmail(usuario, confirmationToken, expirationTime);
            logger.info("[REGISTER] Email enviado exitosamente");
            logger.info("[REGISTER] Registro completado exitosamente para: {}", usuarioDTO.getEmail());
            result = "Usuario registrado exitosamente. Revisa tu email para validar la cuenta";
        } 
        catch (ResponseStatusException e) {
            tryCleanupUsuario(usuario);
            throw new ResponseStatusException(
                e.getStatusCode(),
                "Error durante el registro del usuario: " + e.getReason(),
                e
            );
        }
        catch (IllegalStateException e) {
            logger.error("[REGISTER] Error al enviar el correo de confirmación para {}: {}", usuarioDTO.getEmail(), e.getMessage(), e);
            tryCleanupUsuario(usuario);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Error durante el registro del usuario: " + e.getMessage(),
                e
            );
        } 
        catch (Exception e) {
            logger.error("[REGISTER] Error interno durante el registro de {}: {}", usuarioDTO.getEmail(), e.getMessage(), e);
            tryCleanupUsuario(usuario);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
        return result;
    }

    private void tryCleanupUsuario(UsuarioNormal usuario) {
        if (usuario != null && usuario.getIdUsuario() != null) {
            try {
                usuarioNormalRepository.deleteById(usuario.getIdUsuario());
                logger.info("[REGISTER] Usuario limpiado después de error interno");
            } 
            catch (Exception cleanupException) {
                logger.error("[REGISTER] Error limpiando usuario después de fallo: {}", cleanupException.getMessage());
            }
        }
    }

    /**
     * Registra un nuevo creador de contenido.
     */
    public String registerCreator(CreadorContenidoDTO creadorDTO) throws ResponseStatusException {
        try {
            validationService.validateCreatorSpecific(creadorDTO.getEmail(), creadorDTO.getAliasCreador());
            CreadorContenido creador = (CreadorContenido) usuarioFactory.crearUsuario(creadorDTO, validationService);
            creador.setRol(Rol.CREADOR);
            creador.setTwoFactorEnabled(true);
            creadorContenidoRepository.save(creador);
            
            return "¡Registro enviado! Un administrador tiene que validar tu solicitud de creador de contenido.";
        } 
        catch (ResponseStatusException | IllegalArgumentException e) {
            logger.info("La solicitud de registro de creador de contenido es inválida.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El formato de la solicitud es incorrecto", e);
        }
        catch (Exception e) {
            logger.error("Error interno durante el registro del creador:", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno durante el registro del creador");
        }
    }

    /**
     * Registra un nuevo administrador en el sistema.
     */
    public String registerAdmin(AdministradorDTO adminDTO) throws ResponseStatusException {
        try {
            logger.info("[REGISTER-ADMIN] Iniciando registro de administrador: {}", adminDTO.getEmail());
            validationService.validateAdminUniqueness(adminDTO.getEmail(), adminDTO.getAlias());

            Administrador admin = (Administrador) usuarioFactory.crearUsuario(adminDTO, validationService);
            admin.setDepartamento(adminDTO.getDepartamento());
            admin.setFechaRegistro(new Date());
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setTwoFactorEnabled(true);
            adminRepository.save(admin);
            logger.info("[REGISTER-ADMIN] Administrador registrado exitosamente: {} - Departamento: {}", 
                adminDTO.getEmail(), adminDTO.getDepartamento());
            return "Administrador registrado exitosamente.";
        } 
        catch (ResponseStatusException e) {
            throw new ResponseStatusException(e.getStatusCode(), 
                "Error en registro de administrador: " + e.getReason(), e);
        }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Datos de administrador inválidos: " + e.getMessage(), e);
        }
        catch (Exception e) {
            logger.error("[REGISTER-ADMIN] Error interno durante el registro del administrador {}: {}", 
                adminDTO.getEmail(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error interno durante el registro del administrador");
        }
    }

    /**
     * Valida un token de confirmación y confirma la cuenta
     */
    public String confirmUserAccount(String tokenValue) throws ResponseStatusException {
        try {
            Optional<Token> optionalToken = tokenRepository.findAll().stream()
                .filter(t -> t.getTokenCreado() != null && t.getTokenCreado().equals(tokenValue) && t.getTipoToken() == TipoToken.CONFIRMACION_CUENTA)
                .findFirst();
            if (optionalToken.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de confirmación inválido");
            }
            Token token = optionalToken.get();
            if (!token.isValido()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
            }
            UsuarioNormal usuario = usuarioNormalRepository.findById(token.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            if (usuario.isConfirmado()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cuenta ya está validada");
            }
            usuario.setConfirmado(true);
            usuarioNormalRepository.save(usuario);
            token.setEstado(EstadoToken.UTILIZADA);
            tokenRepository.save(token);
            return "Cuenta validada exitosamente";
        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error interno durante la confirmación:", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno durante la confirmación");
        }
    }

    private void validateNormalUserInput(String email, String alias, String password, Date fechaNacimiento) {
        validationService.validateUsuarioNormalData(email, alias, password, fechaNacimiento);
    }

    private String generateConfirmationToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}