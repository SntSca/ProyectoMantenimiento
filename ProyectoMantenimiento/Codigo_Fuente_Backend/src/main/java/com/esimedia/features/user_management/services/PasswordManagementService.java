package com.esimedia.features.user_management.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Token;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.services.EmailService;
import com.esimedia.features.auth.services.PasswordSecurityService;
import com.esimedia.features.user_management.dto.PasswordChangeDTO;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.TokenRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

@Service
public class PasswordManagementService {

    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    private static final String CREADOR_NO_ENCONTRADO = "Creador no encontrado";
    private static final String TOKEN_INVALIDO = "Token de recuperación inválido";
    private static final String ERROR_INTERNO_RECUPERACION = "Error interno durante la solicitud de recuperación";
    private static final String ERROR_INTERNO_RESET = "Error interno durante el restablecimiento de contraseña";
    private static final String CONTRASENA_ACTUAL_INCORRECTA = "La contraseña actual es incorrecta";
    private static final String CONTRASENA_DIFERENTE_REQUERIDA = "Por favor, elige una contraseña diferente a la actual";
    private static final String CONTRASENA_REUTILIZADA = "La nueva contraseña ya ha sido utilizada recientemente. Por favor, selecciona una contraseña diferente.";
    private static final int PASSWORD_HISTORY_LIMIT = 5;

    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository adminRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordSecurityService passwordSecurityService;
    private final Logger logger;
    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    @Value("${app.security.pepper}")
    private String pepper;

    @Value("${email.resetPasswordUrl}")
    private String resetPasswordUrl;

    public PasswordManagementService(UsuarioNormalRepository usuarioNormalRepository,
                                     CreadorContenidoRepository creadorContenidoRepository,
                                     AdminRepository adminRepository,
                                     TokenRepository tokenRepository,
                                     EmailService emailService,
                                     PasswordSecurityService passwordSecurityService) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.adminRepository = adminRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordSecurityService = passwordSecurityService;
        this.logger = LoggerFactory.getLogger(PasswordManagementService.class);
    }

    public void changePassword(String userId, PasswordChangeDTO passwordChangeDTO) {
        // Buscar el usuario en los tres tipos posibles
        Optional<UsuarioNormal> normalOpt = usuarioNormalRepository.findById(userId);
        if (normalOpt.isPresent()) {
            changePasswordForNormal(normalOpt.get(), passwordChangeDTO);
            return;
        }
        
        Optional<CreadorContenido> creatorOpt = creadorContenidoRepository.findById(userId);
        if (creatorOpt.isPresent()) {
            changePasswordForCreator(creatorOpt.get(), passwordChangeDTO);
            return;
        }
        
        Optional<Administrador> adminOpt = adminRepository.findById(userId);
        if (adminOpt.isPresent()) {
            changePasswordForAdmin(adminOpt.get(), passwordChangeDTO);
            return;
        }
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO);
    }

        private void changePasswordForNormal(UsuarioNormal user, PasswordChangeDTO passwordChangeDTO) {
        String oldPassword = passwordChangeDTO.getOldPassword();
        String newPassword = passwordChangeDTO.getNewPassword();
        
        if (!matchesPassword(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CONTRASENA_ACTUAL_INCORRECTA);
        }

        // 🔒 No permitir reutilizar actual ni historial
        ensureNotReused(user, newPassword);

        validatePasswordStrength(newPassword, user.getAlias(), user.getEmail());
        
        // Guardar contraseña actual en historial
        pushPasswordToHistory(user);

        user.setPassword(encodePassword(newPassword));
        user.setCredentialsVersion(user.getCredentialsVersion() + 1);
        usuarioNormalRepository.save(user);
        
        logger.info("Contraseña cambiada exitosamente para el usuario normal con ID: {}", user.getIdUsuario());
    }


        private void changePasswordForCreator(CreadorContenido user, PasswordChangeDTO passwordChangeDTO) {
        String oldPassword = passwordChangeDTO.getOldPassword();
        String newPassword = passwordChangeDTO.getNewPassword();
        
        if (!matchesPassword(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CONTRASENA_ACTUAL_INCORRECTA);
        }

        // 🔒 No permitir reutilizar actual ni historial
        ensureNotReused(user, newPassword);

        validatePasswordStrength(newPassword, user.getAlias(), user.getEmail());
        
        pushPasswordToHistory(user);

        user.setPassword(encodePassword(newPassword));
        user.setCredentialsVersion(user.getCredentialsVersion() + 1);
        creadorContenidoRepository.save(user);
        
        logger.info("Contraseña cambiada exitosamente para el creador con ID: {}", user.getIdUsuario());
    }


        private void changePasswordForAdmin(Administrador user, PasswordChangeDTO passwordChangeDTO) {
        String oldPassword = passwordChangeDTO.getOldPassword();
        String newPassword = passwordChangeDTO.getNewPassword();
        
        if (!matchesPassword(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CONTRASENA_ACTUAL_INCORRECTA);
        }

        // 🔒 No permitir reutilizar actual ni historial
        ensureNotReused(user, newPassword);

        validatePasswordStrength(newPassword, user.getAlias(), user.getEmail());
        
        pushPasswordToHistory(user);

        user.setPassword(encodePassword(newPassword));
        user.setCredentialsVersion(user.getCredentialsVersion() + 1);
        adminRepository.save(user);
        
        logger.info("Contraseña cambiada exitosamente para el administrador con ID: {}", user.getIdUsuario());
    }


    public String requestPasswordResetGeneric(String email, String userType) {
        try {
            String userId = null;
            String userEmail = null;
            boolean isConfirmed = true;

            switch (userType) {
                case "normal": {
                    UsuarioNormal u = usuarioNormalRepository.findByemail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
                    userId = u.getIdUsuario();
                    userEmail = u.getEmail();
                    isConfirmed = u.isConfirmado();
                    if (!isConfirmed) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes validar tu cuenta antes de poder recuperar la contraseña");
                    }
                    break;
                }
                case "creador": {
                    CreadorContenido c = creadorContenidoRepository.findByemail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO));
                    userId = c.getIdUsuario();
                    userEmail = c.getEmail();
                    isConfirmed = c.isValidado();
                    if (!isConfirmed) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El creador debe ser validado antes de recuperar la contraseña");
                    }
                    break;
                }
                case "admin": {
                    Administrador a = adminRepository.findByemail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
                    userId = a.getIdUsuario();
                    userEmail = a.getEmail();
                    break;
                }
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de usuario no soportado");
            }

            String resetToken = generateResetToken();
            Token token = Token.builder()
                .id(userId)
                .tokenCreado(resetToken)
                .tipoToken(TipoToken.RECUPERACION_PASSWORD)
                .usuarioEmail(userEmail)
                .fechaInicio(LocalDateTime.now())
                .fechaUltimaActividad(LocalDateTime.now())
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();
            tokenRepository.save(token);

            String resetLink = resetPasswordUrl + resetToken;
            String expirationTime = token.getFechaInicio().plusHours(24).format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));
            emailService.resetEmail(userEmail, "Recuperación de contraseña", resetLink, expirationTime);

            logger.info("Token de recuperación generado para usuario {} tipo {}: {}", userEmail, userType, resetToken);
            return "Se ha enviado un enlace de recuperación a tu correo electrónico";
        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error durante la solicitud de recuperación de contraseña:", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO_RECUPERACION);
        }
    }

    @Transactional
    public String resetPassword(String tokenValue, String newPassword) {
        try {
            Optional<Token> resetTokenOpt = tokenRepository.findAll().stream()
                .filter(t -> t.getTokenCreado() != null && t.getTokenCreado().equals(tokenValue) && t.getTipoToken() == TipoToken.RECUPERACION_PASSWORD)
                .findFirst();
            if (resetTokenOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TOKEN_INVALIDO);
            }
            Token resetToken = resetTokenOpt.get();

            if (!resetToken.isValido()) {
                if (resetToken.isExpirado()) {
                    tokenRepository.delete(resetToken);
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TOKEN_INVALIDO);
            }

            String userEmail = resetToken.getUsuarioEmail();
            String userAlias = null;
            boolean userFound = false;

            // Buscar por ID en lugar de email para asegurar la tabla correcta
            Optional<UsuarioNormal> normalOpt = usuarioNormalRepository.findById(resetToken.getId());
            if (normalOpt.isPresent()) {
                UsuarioNormal user = normalOpt.get();
                userAlias = user.getAlias();

                // 🔒 No permitir reutilizar actual ni historial
                ensureNotReused(user, newPassword);

                validatePasswordStrength(newPassword, userAlias, userEmail);

                // Guardar contraseña actual en historial antes de cambiarla
                pushPasswordToHistory(user);

                user.setPassword(encodePassword(newPassword));
                usuarioNormalRepository.save(user);
                logger.info("Contraseña guardada exitosamente para UsuarioNormal: {}", userEmail);
                userFound = true;
            }


            if (!userFound) {
                Optional<CreadorContenido> creadorOpt = creadorContenidoRepository.findById(resetToken.getId());
                if (creadorOpt.isPresent()) {
                    CreadorContenido creador = creadorOpt.get();
                    userAlias = creador.getAlias();

                    ensureNotReused(creador, newPassword);
                    validatePasswordStrength(newPassword, userAlias, userEmail);
                    pushPasswordToHistory(creador);

                    creador.setPassword(encodePassword(newPassword));
                    creadorContenidoRepository.save(creador);
                    logger.info("Contraseña guardada exitosamente para CreadorContenido: {}", userEmail);
                    userFound = true;
                }
            }


            if (!userFound) {
                Optional<Administrador> adminOpt = adminRepository.findById(resetToken.getId());
                if (adminOpt.isPresent()) {
                    Administrador admin = adminOpt.get();
                    userAlias = admin.getAlias();

                    ensureNotReused(admin, newPassword);
                    validatePasswordStrength(newPassword, userAlias, userEmail);
                    pushPasswordToHistory(admin);

                    admin.setPassword(encodePassword(newPassword));
                    adminRepository.save(admin);
                    logger.info("Contraseña guardada exitosamente para Administrador: {}", userEmail);
                    userFound = true;
                }
            }


            if (!userFound) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO);
            }

            tokenRepository.delete(resetToken);

            logger.info("Contraseña restablecida exitosamente para usuario: {}", userEmail);
            return "Contraseña restablecida exitosamente";

        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error durante el restablecimiento de contraseña para token {}: ", tokenValue, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                ERROR_INTERNO_RESET);
        }
    }

    public boolean validateResetToken(String tokenValue) {
        boolean isValid = false;
        try {
            Optional<Token> resetToken = tokenRepository.findAll().stream()
                .filter(t -> t.getTokenCreado() != null && t.getTokenCreado().equals(tokenValue) && t.getTipoToken() == TipoToken.RECUPERACION_PASSWORD)
                .findFirst();
            
            if (resetToken.isPresent()) {
                Token token = resetToken.get();
                
                if (token.isValido()) {
                    isValid = true;
                } 
                else if (token.isExpirado()) {
                    tokenRepository.delete(token);
                }
            }
        } 
        catch (Exception e) {
            logger.error("Error validando token de recuperación:", e);
        }
        return isValid;
    }


    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validatePasswordStrength(String password, String alias, String email) {
        
        if (password.toLowerCase().contains(alias.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña no debe contener el alias de usuario");
        }

        if (email != null && password.toLowerCase().contains(email.split("@")[0].toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña no debe contener el nombre del correo electrónico");
        }
        
        // Verificación contra diccionario de contraseñas comunes
        if (passwordSecurityService.isPasswordInDictionary(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña seleccionada es demasiado común. Por favor, elige una contraseña más segura.");
        }
        
        // Verificación contra brechas de datos públicas
        if (passwordSecurityService.isPasswordLeaked(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña seleccionada ha sido comprometida en brechas de datos públicas. " +
                "Por tu seguridad, por favor selecciona una contraseña diferente.");
        }
        
        logger.info("Validación de contraseña completada exitosamente para usuario con email: {}", email);
    }

    private String encodePassword(String password) throws SecurityException {
        String passwordWithPepper = password + pepper;
        int memory = 65536;
        int iterations = 3;
        int parallelism = 1;
        String hash = argon2.hash(iterations, memory, parallelism, passwordWithPepper.toCharArray());
        argon2.wipeArray(passwordWithPepper.toCharArray());
        return hash;
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        logger.info("Verificando contraseña con pepper: {}", pepper);
        String passwordWithPepper = rawPassword + pepper;
        return argon2.verify(encodedPassword, passwordWithPepper.toCharArray());
    }

    private void ensureNotReused(Usuario user, String newPassword) {
        // 1) No puede ser igual a la actual
        if (matchesPassword(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CONTRASENA_DIFERENTE_REQUERIDA);
        }

        // 2) No puede ser igual a ninguna de las últimas N
        if (user.getPasswordHistory() != null) {
            for (String oldHash : user.getPasswordHistory()) {
                if (matchesPassword(newPassword, oldHash)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CONTRASENA_REUTILIZADA);
                }
            }
        }
    }

        /**
     * Añade la contraseña actual al historial del usuario (máx 5 registros).
     */
    private void pushPasswordToHistory(Usuario user) {
        String currentHash = user.getPassword();
        if (currentHash == null || currentHash.isBlank()) {
            return;
        }

        java.util.List<String> history = user.getPasswordHistory();
        if (history == null) {
            history = new java.util.ArrayList<>();
        }

        // Insertar la contraseña actual al comienzo
        history.add(0, currentHash);

        // Limitar a las 5 más recientes
        if (history.size() > PASSWORD_HISTORY_LIMIT) {
            history = history.subList(0, PASSWORD_HISTORY_LIMIT);
        }

        user.setPasswordHistory(history);
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
    }

}
