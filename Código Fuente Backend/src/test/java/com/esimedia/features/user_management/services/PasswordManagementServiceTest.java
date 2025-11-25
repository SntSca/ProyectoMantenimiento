package com.esimedia.features.user_management.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Token;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.TokenRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.EmailService;
import com.esimedia.features.auth.services.PasswordSecurityService;
import com.esimedia.features.user_management.dto.PasswordChangeDTO;

@ExtendWith(MockitoExtension.class)
class PasswordManagementServiceTest {

    @Mock private UsuarioNormalRepository usuarioNormalRepository;
    @Mock private CreadorContenidoRepository creadorContenidoRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordSecurityService passwordSecurityService;

    @InjectMocks
    private PasswordManagementService passwordManagementService;

    private static final String PEPPER = "testpepper123";
    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_ALIAS = "john_doe";
    private static final String USER_ID = "user123";
    private static final String RESET_TOKEN = "abc123xyz";
    private static final String NEW_PASSWORD = "SecurePass123!";

    // ====== NUEVO: método helper para invocar encodePassword privado ======
    private String invokeEncodePassword(String rawPassword) {
        try {
            Method method = PasswordManagementService.class.getDeclaredMethod("encodePassword", String.class);
            method.setAccessible(true);
            return (String) method.invoke(passwordManagementService, rawPassword);
        } 
        catch (Exception e) {
            throw new RuntimeException("Error invoking encodePassword", e);
        }
    }
    // ======================================================================

    @BeforeEach
    void setUp() throws Exception {
        Field pepperField = PasswordManagementService.class.getDeclaredField("pepper");
        pepperField.setAccessible(true);
        pepperField.set(passwordManagementService, PEPPER);

        Field resetUrlField = PasswordManagementService.class.getDeclaredField("resetPasswordUrl");
        resetUrlField.setAccessible(true);
        resetUrlField.set(passwordManagementService, "https://example.com/reset?token=");

        lenient().when(passwordSecurityService.isPasswordInDictionary(anyString())).thenReturn(false);
        lenient().when(passwordSecurityService.isPasswordLeaked(anyString())).thenReturn(false);
    }

    private Token createValidResetToken() {
        return Token.builder()
                .id(USER_ID)
                .tokenCreado(RESET_TOKEN)
                .tipoToken(TipoToken.RECUPERACION_PASSWORD)
                .usuarioEmail(VALID_EMAIL)
                .fechaInicio(LocalDateTime.now().minusMinutes(10))
                .fechaUltimaActividad(LocalDateTime.now().minusMinutes(10))
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();
    }

    private Token createExpiredResetToken() {
        return Token.builder()
                .id(USER_ID)
                .tokenCreado("expired_token")
                .tipoToken(TipoToken.RECUPERACION_PASSWORD)
                .usuarioEmail(VALID_EMAIL)
                .fechaInicio(LocalDateTime.now().minusHours(2))
                .fechaUltimaActividad(LocalDateTime.now().minusHours(2))
                .estado(EstadoToken.EXPIRADA)
                .build();
    }

    // ========== changePassword - UsuarioNormal ==========

    @Test
    void changePassword_NormalUser_Success() {
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setEmail(VALID_EMAIL);
        user.setAlias(VALID_ALIAS);
        user.setCredentialsVersion(1);
        user.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("OldPass123!");
        dto.setNewPassword("NewSecurePass456!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        passwordManagementService.changePassword(USER_ID, dto);

        verify(usuarioNormalRepository).save(argThat(u -> u.getCredentialsVersion() == 2));
    }

    @Test
    void changePassword_NormalUser_WrongOldPassword() {
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setEmail(VALID_EMAIL);
        user.setAlias(VALID_ALIAS);
        user.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("WrongPassword!");
        dto.setNewPassword("NewSecurePass456!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.changePassword(USER_ID, dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void changePassword_NormalUser_SamePassword() {
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setEmail(VALID_EMAIL);
        user.setAlias(VALID_ALIAS);
        user.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("OldPass123!");
        dto.setNewPassword("OldPass123!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.changePassword(USER_ID, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ========== changePassword - CreadorContenido ==========

    @Test
    void changePassword_CreadorUser_Success() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEmail(VALID_EMAIL);
        creador.setAlias(VALID_ALIAS);
        creador.setCredentialsVersion(1);
        creador.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("OldPass123!");
        dto.setNewPassword("NewSecurePass456!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));

        passwordManagementService.changePassword(USER_ID, dto);

        verify(creadorContenidoRepository).save(argThat(c -> c.getCredentialsVersion() == 2));
    }

    @Test
    void changePassword_CreadorUser_WrongOldPassword() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEmail(VALID_EMAIL);
        creador.setAlias(VALID_ALIAS);
        creador.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("WrongPassword!");
        dto.setNewPassword("NewSecurePass456!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.changePassword(USER_ID, dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void changePassword_CreadorUser_SamePassword() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEmail(VALID_EMAIL);
        creador.setAlias(VALID_ALIAS);
        creador.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("OldPass123!");
        dto.setNewPassword("OldPass123!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.changePassword(USER_ID, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ========== changePassword - Administrador ==========

    @Test
    void changePassword_AdminUser_Success() {
        Administrador admin = new Administrador();
        admin.setIdUsuario(USER_ID);
        admin.setEmail(VALID_EMAIL);
        admin.setAlias(VALID_ALIAS);
        admin.setCredentialsVersion(1);
        admin.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("OldPass123!");
        dto.setNewPassword("NewSecurePass456!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        passwordManagementService.changePassword(USER_ID, dto);

        verify(adminRepository).save(argThat(a -> a.getCredentialsVersion() == 2));
    }

    @Test
    void changePassword_AdminUser_WrongOldPassword() {
        Administrador admin = new Administrador();
        admin.setIdUsuario(USER_ID);
        admin.setEmail(VALID_EMAIL);
        admin.setAlias(VALID_ALIAS);
        admin.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("WrongPassword!");
        dto.setNewPassword("NewSecurePass456!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.changePassword(USER_ID, dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void changePassword_AdminUser_SamePassword() {
        Administrador admin = new Administrador();
        admin.setIdUsuario(USER_ID);
        admin.setEmail(VALID_EMAIL);
        admin.setAlias(VALID_ALIAS);
        admin.setPassword(invokeEncodePassword("OldPass123!"));

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("OldPass123!");
        dto.setNewPassword("OldPass123!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.changePassword(USER_ID, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void changePassword_UserNotFound() {
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("OldPass123!");
        dto.setNewPassword("NewSecurePass456!");

        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.changePassword(USER_ID, dto));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ========== requestPasswordResetGeneric ==========

    @Test
    void requestPasswordResetGeneric_NormalUser_Success() {
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setEmail(VALID_EMAIL);
        user.setConfirmado(true);

        when(usuarioNormalRepository.findByemail(VALID_EMAIL)).thenReturn(Optional.of(user));

        String result = passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "normal");

        assertTrue(result.contains("enlace de recuperación"));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void requestPasswordResetGeneric_CreadorUser_Success() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEmail(VALID_EMAIL);
        creador.setValidado(true);

        when(creadorContenidoRepository.findByemail(VALID_EMAIL)).thenReturn(Optional.of(creador));

        String result = passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "creador");

        assertTrue(result.contains("enlace de recuperación"));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void requestPasswordResetGeneric_AdminUser_Success() {
        Administrador admin = new Administrador();
        admin.setIdUsuario(USER_ID);
        admin.setEmail(VALID_EMAIL);

        when(adminRepository.findByemail(VALID_EMAIL)).thenReturn(Optional.of(admin));

        String result = passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "admin");

        assertTrue(result.contains("enlace de recuperación"));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void requestPasswordResetGeneric_NormalUser_NotConfirmed() {
        UsuarioNormal user = new UsuarioNormal();
        user.setEmail(VALID_EMAIL);
        user.setConfirmado(false);

        when(usuarioNormalRepository.findByemail(VALID_EMAIL))
                .thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "normal"));
    }

    @Test
    void requestPasswordResetGeneric_CreadorUser_NotValidated() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEmail(VALID_EMAIL);
        creador.setValidado(false);

        when(creadorContenidoRepository.findByemail(VALID_EMAIL))
                .thenReturn(Optional.of(creador));

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "creador"));
    }

    @Test
    void requestPasswordResetGeneric_NormalUserNotFound() {
        when(usuarioNormalRepository.findByemail(VALID_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "normal"));
    }

    @Test
    void requestPasswordResetGeneric_CreadorNotFound() {
        when(creadorContenidoRepository.findByemail(VALID_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "creador"));
    }

    @Test
    void requestPasswordResetGeneric_AdminNotFound() {
        when(adminRepository.findByemail(VALID_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "admin"));
    }

    @Test
    void requestPasswordResetGeneric_InvalidUserType() {
        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "invalid"));
    }

    @Test
    void requestPasswordResetGeneric_InternalError() {
        when(usuarioNormalRepository.findByemail(VALID_EMAIL))
                .thenThrow(new RuntimeException("Error"));

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.requestPasswordResetGeneric(VALID_EMAIL, "normal"));
    }

    // ========== resetPassword ==========

    @Test
    void resetPassword_ValidToken_NormalUser() {
        Token token = createValidResetToken();
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setAlias(VALID_ALIAS);
        user.setEmail(VALID_EMAIL);

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        String result = passwordManagementService.resetPassword(RESET_TOKEN, NEW_PASSWORD);

        assertEquals("Contraseña restablecida exitosamente", result);
        verify(usuarioNormalRepository).save(any());
        verify(tokenRepository).delete(token);
    }

    @Test
    void resetPassword_ValidToken_Creador() {
        Token token = createValidResetToken();
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setAlias(VALID_ALIAS);
        creador.setEmail(VALID_EMAIL);

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));

        String result = passwordManagementService.resetPassword(RESET_TOKEN, NEW_PASSWORD);

        assertEquals("Contraseña restablecida exitosamente", result);
        verify(creadorContenidoRepository).save(any());
    }

    @Test
    void resetPassword_ValidToken_Admin() {
        Token token = createValidResetToken();
        Administrador admin = new Administrador();
        admin.setIdUsuario(USER_ID);
        admin.setAlias(VALID_ALIAS);
        admin.setEmail(VALID_EMAIL);

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        String result = passwordManagementService.resetPassword(RESET_TOKEN, NEW_PASSWORD);

        assertEquals("Contraseña restablecida exitosamente", result);
        verify(adminRepository).save(any());
    }

    @Test
    void resetPassword_InvalidToken() {
        when(tokenRepository.findAll()).thenReturn(Arrays.asList());

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword("invalid", NEW_PASSWORD));
    }

    @Test
    void resetPassword_ExpiredToken() {
        Token expired = createExpiredResetToken();
        when(tokenRepository.findAll()).thenReturn(Arrays.asList(expired));

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword("expired_token", NEW_PASSWORD));
        verify(tokenRepository).delete(expired);
    }

    @Test
    void resetPassword_UserNotFound() {
        Token token = createValidResetToken();
        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword(RESET_TOKEN, NEW_PASSWORD));
    }

    @Test
    void resetPassword_InternalError() {
        Token token = createValidResetToken();
        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenThrow(new RuntimeException("Error"));

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword(RESET_TOKEN, NEW_PASSWORD));
    }

    @Test
    void resetPassword_PasswordContainsAlias() {
        Token token = createValidResetToken();
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setAlias("johndoe");
        user.setEmail(VALID_EMAIL);

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword(RESET_TOKEN, "johndoe123!"));
    }

    @Test
    void resetPassword_PasswordContainsEmail() {
        Token token = createValidResetToken();
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setAlias(VALID_ALIAS);
        user.setEmail("testuser@example.com");

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword(RESET_TOKEN, "testuser123!"));
    }

    @Test
    void resetPassword_PasswordInDictionary() {
        Token token = createValidResetToken();
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setAlias(VALID_ALIAS);
        user.setEmail(VALID_EMAIL);

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordSecurityService.isPasswordInDictionary("password123")).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword(RESET_TOKEN, "password123"));
    }

    @Test
    void resetPassword_PasswordIsLeaked() {
        Token token = createValidResetToken();
        UsuarioNormal user = new UsuarioNormal();
        user.setIdUsuario(USER_ID);
        user.setAlias(VALID_ALIAS);
        user.setEmail(VALID_EMAIL);

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordSecurityService.isPasswordLeaked("leakedpass123")).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> passwordManagementService.resetPassword(RESET_TOKEN, "leakedpass123"));
    }

    // ========== validateResetToken ==========

    @Test
    void validateResetToken_Valid() {
        Token token = createValidResetToken();
        when(tokenRepository.findAll()).thenReturn(Arrays.asList(token));

        assertTrue(passwordManagementService.validateResetToken(RESET_TOKEN));
    }

    @Test
    void validateResetToken_Expired() {
        Token expired = createExpiredResetToken();
        when(tokenRepository.findAll()).thenReturn(Arrays.asList(expired));

        assertFalse(passwordManagementService.validateResetToken("expired_token"));
        verify(tokenRepository).delete(expired);
    }

    @Test
    void validateResetToken_Invalid() {
        when(tokenRepository.findAll()).thenReturn(Arrays.asList());

        assertFalse(passwordManagementService.validateResetToken("invalid"));
    }

    @Test
    void validateResetToken_WrongType() {
        Token wrongType = Token.builder()
                .id(USER_ID)
                .tokenCreado(RESET_TOKEN)
                .tipoToken(TipoToken.CONFIRMACION_CUENTA)
                .usuarioEmail(VALID_EMAIL)
                .fechaInicio(LocalDateTime.now().minusMinutes(10))
                .fechaUltimaActividad(LocalDateTime.now().minusMinutes(10))
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();

        when(tokenRepository.findAll()).thenReturn(Arrays.asList(wrongType));

        assertFalse(passwordManagementService.validateResetToken(RESET_TOKEN));
    }

    @Test
    void validateResetToken_Exception() {
        when(tokenRepository.findAll()).thenThrow(new RuntimeException("Error"));

        assertFalse(passwordManagementService.validateResetToken(RESET_TOKEN));
    }
}
