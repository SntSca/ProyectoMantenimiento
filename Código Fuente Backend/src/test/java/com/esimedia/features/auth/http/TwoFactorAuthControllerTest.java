package com.esimedia.features.auth.http;

import com.esimedia.features.auth.dto.TotpSetupResponse;
import com.esimedia.features.auth.services.TwoFactorAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TwoFactorAuthControllerTest {

    private MockMvc mockMvc;
    private TwoFactorAuthService twoFactorAuthService;

    @BeforeEach
    void setup() {
        twoFactorAuthService = Mockito.mock(TwoFactorAuthService.class);
        TwoFactorAuthController controller = new TwoFactorAuthController(twoFactorAuthService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ===================== SEND EMAIL CODE =====================
    @Test
    void sendEmailCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.sendEmailVerificationCode("123"))
                .thenReturn("Código enviado al correo electrónico");

        mockMvc.perform(post("/auth/2fa/email/send")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código enviado al correo electrónico"));

        verify(twoFactorAuthService).validarEntradaPara2O3FA("Bearer token123");
        verify(twoFactorAuthService).sendEmailVerificationCode("123");
    }

    @Test
    void sendEmailCode_unauthorized() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/2fa/email/send")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().is4xxClientError());
    }

    // ===================== VERIFY EMAIL CODE =====================
    @Test
    void verifyEmailCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.verifyEmailCode("123", "123456")).thenReturn(true);

        mockMvc.perform(post("/auth/2fa/email/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código verificado exitosamente"));

        verify(twoFactorAuthService).verifyEmailCode("123", "123456");
    }

    @Test
    void verifyEmailCode_invalid() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.verifyEmailCode("123", "000000")).thenReturn(false);

        mockMvc.perform(post("/auth/2fa/email/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"000000\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyEmailCode_missingCode() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");

        mockMvc.perform(post("/auth/2fa/email/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyEmailCode_emptyCode() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");

        mockMvc.perform(post("/auth/2fa/email/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    // ===================== SETUP TOTP =====================
    @Test
    void setupTotp_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        
        TotpSetupResponse setupResponse = new TotpSetupResponse();
        setupResponse.setQrCodeUrl("data:image/png;base64,iVBORw0KGgo...");
        setupResponse.setSecretKey("JBSWY3DPEHPK3PXP");
        
        when(twoFactorAuthService.setupTotp("123")).thenReturn(setupResponse);

        mockMvc.perform(post("/auth/2fa/totp/setup")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCodeUrl").value("data:image/png;base64,iVBORw0KGgo..."))
                .andExpect(jsonPath("$.secretKey").value("JBSWY3DPEHPK3PXP"))
                .andExpect(jsonPath("$.message").exists());

        verify(twoFactorAuthService).setupTotp("123");
    }

    @Test
    void setupTotp_unauthorized() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/2fa/totp/setup")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().is4xxClientError());
    }

    // ===================== CONFIRM TOTP SETUP =====================
    @Test
    void confirmTotpSetup_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.confirmTotpSetup("123", "123456"))
                .thenReturn("TOTP configurado exitosamente");

        mockMvc.perform(post("/auth/2fa/totp/confirm")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("TOTP configurado exitosamente"));

        verify(twoFactorAuthService).confirmTotpSetup("123", "123456");
    }

    @Test
    void confirmTotpSetup_missingCode() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");

        mockMvc.perform(post("/auth/2fa/totp/confirm")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmTotpSetup_emptyCode() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");

        mockMvc.perform(post("/auth/2fa/totp/confirm")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmTotpSetup_invalidCode() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.confirmTotpSetup("123", "000000"))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, 
                        "Código TOTP inválido"));

        mockMvc.perform(post("/auth/2fa/totp/confirm")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"000000\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ===================== VERIFY TOTP CODE =====================
    @Test
    void verifyTotpCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.verifyTotpCode("123", "111111")).thenReturn(true);

        mockMvc.perform(post("/auth/2fa/totp/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"111111\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("verificado")));

        verify(twoFactorAuthService).validarEntradaPara2O3FA("Bearer token123");
        verify(twoFactorAuthService).verifyTotpCode("123", "111111");
    }

    @Test
    void verifyTotpCode_invalid() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.verifyTotpCode("123", "000000")).thenReturn(false);

        mockMvc.perform(post("/auth/2fa/totp/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"000000\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyTotpCode_missingCode() throws Exception {
        mockMvc.perform(post("/auth/2fa/totp/verify")
                        .requestAttr("userId", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyTotpCode_emptyCode() throws Exception {
        mockMvc.perform(post("/auth/2fa/totp/verify")
                        .requestAttr("userId", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyTotpCode_noUserId() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/2fa/totp/verify")
                        .header("Authorization", "Bearer invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"111111\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== VERIFY BACKUP CODE =====================
/* 
        @Test
        void verifyBackupCode_missingCode() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");

        mockMvc.perform(post("/auth/2fa/backup/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyBackupCode_emptyCode() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");

        mockMvc.perform(post("/auth/2fa/backup/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"   \"}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyBackupCode_noUserId() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/2fa/backup/verify")
                        .header("Authorization", "Bearer invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"BACKUP123\"}"))
                .andExpect(status().isUnauthorized());
        }
*/
    // ===================== DISABLE TWO FACTOR =====================
    @Test
    void disableTwoFactor_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.disableTwoFactor("123"))
                .thenReturn("Autenticación de dos factores desactivada");

        mockMvc.perform(post("/auth/2fa/disable")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Autenticación de dos factores desactivada"));

        verify(twoFactorAuthService).validarEntradaPara2O3FA("Bearer token123");
        verify(twoFactorAuthService).disableTwoFactor("123");
    }

    @Test
    void disableTwoFactor_noUserId() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/2fa/disable")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disableTwoFactor_serviceError() throws Exception {
        when(twoFactorAuthService.disableTwoFactor("123"))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, 
                        "2FA no está activado"));

        mockMvc.perform(post("/auth/2fa/disable")
                        .requestAttr("userId", "123"))
                .andExpect(status().is4xxClientError());
    }

    // ===================== GET USER ID FROM REQUEST =====================
    @Test
    void getUserIdFromRequest_success() throws Exception {
        // Este método privado se testea indirectamente a través de otros endpoints
        // Ya está cubierto por los tests de verifyTotpCode, verifyBackupCode y disableTwoFactor
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.verifyTotpCode("123", "111111")).thenReturn(true);

        mockMvc.perform(post("/auth/2fa/totp/verify")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"111111\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserIdFromRequest_nullUserId() throws Exception {
        // Verifica el comportamiento cuando userId es null
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/2fa/totp/verify")
                        .header("Authorization", "Bearer invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"111111\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== DISABLE TOTP =====================
    @Test
    void disableTotp_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("123");
        when(twoFactorAuthService.disableThirdFactor("123"))
                .thenReturn("3FA desactivado exitosamente");

        mockMvc.perform(post("/auth/2fa/totp/disable")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("3FA desactivado exitosamente"));

        verify(twoFactorAuthService).validarEntradaPara2O3FA("Bearer token123");
        verify(twoFactorAuthService).disableThirdFactor("123");
    }

    // ===================== ADMIN ENDPOINTS =====================
    @Test
    void adminSendEmailCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("admin123");
        when(twoFactorAuthService.sendEmailVerificationCode("admin123"))
                .thenReturn("Código enviado al correo electrónico");

        mockMvc.perform(post("/auth/2fa/admin/email/send")
                        .header("Authorization", "Bearer adminToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código enviado al correo electrónico"));

        verify(twoFactorAuthService).sendEmailVerificationCode("admin123");
    }

    @Test
    void adminVerifyEmailCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("admin123");
        when(twoFactorAuthService.verifyEmailCodeAdmin("admin123", "123456")).thenReturn(true);

        mockMvc.perform(post("/auth/2fa/admin/email/verify")
                        .header("Authorization", "Bearer adminToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código verificado exitosamente"));

        verify(twoFactorAuthService).verifyEmailCodeAdmin("admin123", "123456");
    }

    @Test
    void adminSetupTotp_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("admin123");
        
        TotpSetupResponse setupResponse = new TotpSetupResponse();
        setupResponse.setQrCodeUrl("data:image/png;base64,adminQR");
        setupResponse.setSecretKey("ADMINSECRET");
        
        when(twoFactorAuthService.setupTotpAdmin("admin123")).thenReturn(setupResponse);

        mockMvc.perform(post("/auth/2fa/admin/totp/setup")
                        .header("Authorization", "Bearer adminToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCodeUrl").value("data:image/png;base64,adminQR"))
                .andExpect(jsonPath("$.secretKey").value("ADMINSECRET"));

        verify(twoFactorAuthService).setupTotpAdmin("admin123");
    }

    @Test
    void adminConfirmTotpSetup_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("admin123");
        when(twoFactorAuthService.confirmTotpSetupAdmin("admin123", "123456"))
                .thenReturn("TOTP configurado exitosamente");

        mockMvc.perform(post("/auth/2fa/admin/totp/confirm")
                        .header("Authorization", "Bearer adminToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("TOTP configurado exitosamente"));

        verify(twoFactorAuthService).confirmTotpSetupAdmin("admin123", "123456");
    }

    @Test
    void adminVerifyTotpCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("admin123");
        when(twoFactorAuthService.verifyTotpCodeAdmin("admin123", "111111")).thenReturn(true);

        mockMvc.perform(post("/auth/2fa/admin/totp/verify")
                        .header("Authorization", "Bearer adminToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"111111\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("verificado")));

        verify(twoFactorAuthService).verifyTotpCodeAdmin("admin123", "111111");
    }

    // ===================== CREATOR ENDPOINTS =====================
    @Test
    void creatorSendEmailCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("creator123");
        when(twoFactorAuthService.sendEmailVerificationCode("creator123"))
                .thenReturn("Código enviado al correo electrónico");

        mockMvc.perform(post("/auth/2fa/creator/email/send")
                        .header("Authorization", "Bearer creatorToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código enviado al correo electrónico"));

        verify(twoFactorAuthService).sendEmailVerificationCode("creator123");
    }

    @Test
    void creatorVerifyEmailCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("creator123");
        when(twoFactorAuthService.verifyEmailCodeCreador("creator123", "123456")).thenReturn(true);

        mockMvc.perform(post("/auth/2fa/creator/email/verify")
                        .header("Authorization", "Bearer creatorToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código verificado exitosamente"));

        verify(twoFactorAuthService).verifyEmailCodeCreador("creator123", "123456");
    }

    @Test
    void creatorSetupTotp_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("creator123");
        
        TotpSetupResponse setupResponse = new TotpSetupResponse();
        setupResponse.setQrCodeUrl("data:image/png;base64,creatorQR");
        setupResponse.setSecretKey("CREATORSECRET");
        
        when(twoFactorAuthService.setupTotpCreador("creator123")).thenReturn(setupResponse);

        mockMvc.perform(post("/auth/2fa/creator/totp/setup")
                        .header("Authorization", "Bearer creatorToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCodeUrl").value("data:image/png;base64,creatorQR"))
                .andExpect(jsonPath("$.secretKey").value("CREATORSECRET"));

        verify(twoFactorAuthService).setupTotpCreador("creator123");
    }

    @Test
    void creatorConfirmTotpSetup_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("creator123");
        when(twoFactorAuthService.confirmTotpSetupCreador("creator123", "123456"))
                .thenReturn("TOTP configurado exitosamente");

        mockMvc.perform(post("/auth/2fa/creator/totp/confirm")
                        .header("Authorization", "Bearer creatorToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("TOTP configurado exitosamente"));

        verify(twoFactorAuthService).confirmTotpSetupCreador("creator123", "123456");
    }

    @Test
    void creatorVerifyTotpCode_success() throws Exception {
        when(twoFactorAuthService.validarEntradaPara2O3FA(anyString())).thenReturn("creator123");
        when(twoFactorAuthService.verifyTotpCodeCreador("creator123", "111111")).thenReturn(true);

        mockMvc.perform(post("/auth/2fa/creator/totp/verify")
                        .header("Authorization", "Bearer creatorToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"111111\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("verificado")));

        verify(twoFactorAuthService).verifyTotpCodeCreador("creator123", "111111");
    }
}

