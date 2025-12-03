package com.esimedia.features.auth.services;

import com.esimedia.features.auth.dto.*;
import com.esimedia.features.auth.entity.*;
import com.esimedia.features.auth.repository.*;
import com.esimedia.shared.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.*;

@Service
public class TwoFactorAuthService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado";

    private final EmailTwoFactorService emailTwoFactorService;
    private final TotpTwoFactorService totpTwoFactorService;
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final AdminRepository adminRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;    
    private final JwtValidationUtil jwtValidationService;

    public TwoFactorAuthService(EmailTwoFactorService emailTwoFactorService,
                                TotpTwoFactorService totpTwoFactorService,
                                UsuarioNormalRepository usuarioNormalRepository,
                                AdminRepository adminRepository,
                                CreadorContenidoRepository creadorContenidoRepository,
                                JwtValidationUtil jwtValidationService) {
        this.emailTwoFactorService = emailTwoFactorService;
        this.totpTwoFactorService = totpTwoFactorService;
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.adminRepository = adminRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.jwtValidationService = jwtValidationService;
    }

    /**
     * Genera y envía un código de verificación por email (soporta los 3 tipos de usuario)
     */
    public String sendEmailVerificationCode(String userId) {
        return emailTwoFactorService.sendEmailVerificationCode(userId);
    }
    
    /**
     * Verifica un código enviado por email
     */
    public boolean verifyEmailCode(String userId, String codigo) {
        if (emailTwoFactorService.verifyEmailCode(userId, codigo)) {
            UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            usuario.setTwoFactorEnabled(true);
            usuarioNormalRepository.save(usuario);

            return true;
        }
        return false;
    }

    /**
     * Verifica un código enviado por email para administrador
     */
    public boolean verifyEmailCodeAdmin(String userId, String codigo) {
        if (emailTwoFactorService.verifyEmailCode(userId, codigo)) {
            Administrador usuario = adminRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

            usuario.setTwoFactorEnabled(true);
            adminRepository.save(usuario);

            return true;
        }
        return false;
    }

    public boolean verifyEmailCodeCreador(String userId, String codigo) {
        if (emailTwoFactorService.verifyEmailCode(userId, codigo)) {
            CreadorContenido usuario = creadorContenidoRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
            usuario.setTwoFactorEnabled(true);
            creadorContenidoRepository.save(usuario);
            return true;
        }
        return false;
    }

    // ===== FACTOR 3: TOTP (CÓDIGOS QR) =====

    /**
     * Configura TOTP para un usuario normal - Paso 1: Genera secret y QR
     */
    public TotpSetupResponse setupTotp(String userId) {
        return totpTwoFactorService.setupTotp(userId);
    }
    
    /**
     * Configura TOTP para un administrador - Paso 1: Genera secret y QR
     */
    public TotpSetupResponse setupTotpAdmin(String userId) {
        return totpTwoFactorService.setupTotpAdmin(userId);
    }
    
    /**
     * Configura TOTP para un creador de contenido - Paso 1: Genera secret y QR
     */
    public TotpSetupResponse setupTotpCreador(String userId) {
        return totpTwoFactorService.setupTotpCreador(userId);
    }

    /**
     * Confirma la configuración TOTP para usuario normal - Paso 2: Verifica código inicial
     */
    public String confirmTotpSetup(String userId, String verificationCode) {
        return totpTwoFactorService.confirmTotpSetup(userId, verificationCode);
    }
    
    /**
     * Confirma la configuración TOTP para administrador - Paso 2: Verifica código inicial
     */
    public String confirmTotpSetupAdmin(String userId, String verificationCode) {
        return totpTwoFactorService.confirmTotpSetupAdmin(userId, verificationCode);
    }
    
    /**
     * Confirma la configuración TOTP para creador de contenido - Paso 2: Verifica código inicial
     */
    public String confirmTotpSetupCreador(String userId, String verificationCode) {
        return totpTwoFactorService.confirmTotpSetupCreador(userId, verificationCode);
    }

    public boolean verifyTotpCode(String userId, String code) {
        return totpTwoFactorService.verifyTotpCode(userId, code);
    }

    /**
     * Verifica un código TOTP durante el login para administrador
     */
    public boolean verifyTotpCodeAdmin(String userId, String code) {
        return totpTwoFactorService.verifyTotpCodeAdmin(userId, code);
    }

    /**
     * Verifica un código TOTP durante el login para creador de contenido
     */
    public boolean verifyTotpCodeCreador(String userId, String code) {
        return totpTwoFactorService.verifyTotpCodeCreador(userId, code);
    }

    /**
     * Desactiva 2FA para un usuario
     */
    public String disableTwoFactor(String userId) {
        return totpTwoFactorService.disableTwoFactor(userId);
    }

    public String disableThirdFactor(String userId) {
        return totpTwoFactorService.disableThirdFactor(userId);
    }

    // ===== MÉTODOS DE UTILIDAD =====













    public String validarEntradaPara2O3FA(String authHeader) {
        return jwtValidationService.validarGenerico(authHeader);
    }
}
