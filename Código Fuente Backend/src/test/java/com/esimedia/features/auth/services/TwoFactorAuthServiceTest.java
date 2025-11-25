package com.esimedia.features.auth.services;

import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CodigoVerificacionRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.shared.util.JwtValidationUtil;

class TwoFactorAuthServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private CodigoVerificacionRepository codigoRepo;

    @Mock
    private UsuarioNormalRepository usuarioRepo;

    @Mock
    private AdminRepository adminRepo;

    @Mock
    private CreadorContenidoRepository creadorRepo;

    @Mock
    private UserRetrievalService userRetrievalService;

    @Mock
    private JwtValidationUtil jwtValidationService;

    @Mock
    private EmailTwoFactorService emailTwoFactorService;

    @Mock
    private TotpTwoFactorService totpTwoFactorService;

    @InjectMocks
    private TwoFactorAuthService service;

    private UsuarioNormal usuario;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        usuario = new UsuarioNormal();
        usuario.setIdUsuario("user123");
        usuario.setEmail("test@example.com");

        // Mock TOTP service
        when(totpTwoFactorService.setupTotp(anyString())).thenReturn(new com.esimedia.features.auth.dto.TotpSetupResponse("data:image/png;base64,test", "SECRET123"));
        when(totpTwoFactorService.confirmTotpSetup(anyString(), anyString())).thenReturn("TOTP activated");
        when(totpTwoFactorService.verifyTotpCode(anyString(), anyString())).thenReturn(true);
        when(totpTwoFactorService.disableTwoFactor(anyString())).thenReturn("Disabled");

        // Similar for admin and creador if needed
        when(totpTwoFactorService.setupTotpAdmin(anyString())).thenReturn(new com.esimedia.features.auth.dto.TotpSetupResponse("data:image/png;base64,test", "SECRET123"));
        when(totpTwoFactorService.confirmTotpSetupAdmin(anyString(), anyString())).thenReturn("TOTP activated");
        when(totpTwoFactorService.verifyTotpCodeAdmin(anyString(), anyString())).thenReturn(true);

        when(totpTwoFactorService.setupTotpCreador(anyString())).thenReturn(new com.esimedia.features.auth.dto.TotpSetupResponse("data:image/png;base64,test", "SECRET123"));
        when(totpTwoFactorService.confirmTotpSetupCreador(anyString(), anyString())).thenReturn("TOTP activated");
        when(totpTwoFactorService.verifyTotpCodeCreador(anyString(), anyString())).thenReturn(true);

        // Mock email service
        when(emailTwoFactorService.sendEmailVerificationCode(anyString())).thenReturn("Código de verificación enviado a te***@example.com");
        when(emailTwoFactorService.verifyEmailCode(anyString(), anyString())).thenReturn(true);

        // Mock user retrieval service
        when(userRetrievalService.findAnyUserById(anyString())).thenReturn(Optional.of(usuario));

        // Mock repositories for TOTP setup
        when(usuarioRepo.findById(anyString())).thenReturn(Optional.of(usuario));
        when(adminRepo.findById(anyString())).thenReturn(Optional.of(new Administrador()));
        when(creadorRepo.findById(anyString())).thenReturn(Optional.of(new CreadorContenido()));
    }

    // ========== EMAIL 2FA ==========

    // ========== TOTP ==========

    @Test
    void confirmTotpSetup_invalidCode_throws() {
        usuario.setTotpSecret("SECRET");
        when(totpTwoFactorService.confirmTotpSetup(anyString(), anyString())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));

        assertThrows(ResponseStatusException.class,
                () -> service.confirmTotpSetup("user123", "000000"));
    }

    @Test
    void verifyTotpCode_success() {
        usuario.setTotpSecret("SECRET");
        usuario.setTwoFactorEnabled(true);
        when(totpTwoFactorService.verifyTotpCode(anyString(), anyString())).thenReturn(true);

        boolean result = service.verifyTotpCode("user123", "123456");
        assertTrue(result);
    }

    @Test
    void verifyTotpCode_invalid_returnsFalse() {
        usuario.setTotpSecret("SECRET");
        usuario.setTwoFactorEnabled(true);
        when(totpTwoFactorService.verifyTotpCode(anyString(), anyString())).thenReturn(false);

        assertFalse(service.verifyTotpCode("user123", "000000"));
    }

    // ========== TESTS PARA ADMINISTRADORES ==========

    

    @Test
    void verifyTotpCodeAdmin_success() {
        Administrador admin = new Administrador();
        admin.setIdUsuario("admin123");
        admin.setEmail("admin@example.com");
        admin.setTotpSecret("SECRET");
        admin.setTwoFactorEnabled(true);
        
        when(adminRepo.findById("admin123")).thenReturn(Optional.of(admin));
        when(totpTwoFactorService.verifyTotpCodeAdmin(anyString(), anyString())).thenReturn(true);

        boolean result = service.verifyTotpCodeAdmin("admin123", "123456");

        assertTrue(result);
    }

    // ========== TESTS PARA CREADORES ==========

    @Test
    void verifyTotpCodeCreador_success() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario("creador123");
        creador.setEmail("creador@example.com");
        creador.setTotpSecret("SECRET");
        creador.setTwoFactorEnabled(true);
        
        when(creadorRepo.findById("creador123")).thenReturn(Optional.of(creador));
        when(totpTwoFactorService.verifyTotpCodeCreador(anyString(), anyString())).thenReturn(true);

        boolean result = service.verifyTotpCodeCreador("creador123", "123456");

        assertTrue(result);
    }

    // ========== TESTS ADICIONALES ==========

    @Test
    void validarEntradaPara2O3FA_success() {
        String authHeader = "Bearer token123";
        when(jwtValidationService.validarGenerico(authHeader)).thenReturn("user123");

        String result = service.validarEntradaPara2O3FA(authHeader);

        assertEquals("user123", result);
        verify(jwtValidationService).validarGenerico(authHeader);
    }
}
