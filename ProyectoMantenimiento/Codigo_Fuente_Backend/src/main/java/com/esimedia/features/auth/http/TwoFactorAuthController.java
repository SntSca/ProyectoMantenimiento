package com.esimedia.features.auth.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.services.TwoFactorAuthService;
import com.esimedia.features.auth.dto.CodeVerificationDTO;
import com.esimedia.features.auth.dto.TotpSetupResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth/2fa")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class TwoFactorAuthController {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthController.class);
    private static final String MESSAGE_KEY = "message";
    
    // Response messages
    private static final String CODE_VERIFIED_SUCCESSFULLY = "Código verificado exitosamente";
    private static final String CODE_INVALID_OR_EXPIRED = "Código inválido o expirado";
    private static final String TOTP_CODE_VERIFIED_SUCCESSFULLY = "Código TOTP verificado exitosamente";
    private static final String TOTP_CODE_INVALID = "Código TOTP inválido";
    private static final String QR_SETUP_MESSAGE = "Escanea el código QR con tu aplicación de autenticación y confirma con un código";
    
    // Response keys
    private static final String QR_CODE_URL_KEY = "qrCodeUrl";
    private static final String SECRET_KEY = "secretKey";
    
    private final TwoFactorAuthService twoFactorAuthService;    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }



    // ===== FACTOR 2: CÓDIGOS POR EMAIL =====

    /**
     * Envía un código de verificación por email al usuario autenticado
     */
    @PostMapping("/email/send")
    public ResponseEntity<Map<String, String>> sendEmailCode(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        
        logger.info("Usuario {} solicitó código por email", userId);
        String message = twoFactorAuthService.sendEmailVerificationCode(userId);
        
        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }

    /**
     * Verifica un código enviado por email
     */
    @PostMapping("/email/verify")
    public ResponseEntity<Map<String, String>> verifyEmailCode(
            @RequestHeader("Authorization") String authHeader, 
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();
        
        boolean isValid = twoFactorAuthService.verifyEmailCode(userId, code);
        
        if (isValid) {
            logger.info("Código de email verificado exitosamente para usuario {}", userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, CODE_VERIFIED_SUCCESSFULLY));
        } 
        else 
        {
            logger.warn("Código de email inválido para usuario {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CODE_INVALID_OR_EXPIRED);
        }
    }

    // ===== FACTOR 3: TOTP (CÓDIGOS QR) =====

    /**
     * Configura TOTP para el usuario - Genera QR
     */
    @PostMapping("/totp/setup")
    public ResponseEntity<Map<String, Object>> setupTotp(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);

        logger.info("Usuario {} iniciando configuración TOTP", userId);
        TotpSetupResponse response = twoFactorAuthService.setupTotp(userId);
        
        return ResponseEntity.ok(Map.of(
            QR_CODE_URL_KEY, response.getQrCodeUrl(),
            SECRET_KEY, response.getSecretKey(),
            MESSAGE_KEY, QR_SETUP_MESSAGE
        ));
    }

    /**
     * Confirma la configuración TOTP con el primer código
     */
    @PostMapping("/totp/confirm")
    public ResponseEntity<Map<String, String>> confirmTotpSetup(
            @RequestHeader("Authorization") String authHeader, 
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();
        
        logger.info("Usuario {} confirmando configuración TOTP", userId);
        String message = twoFactorAuthService.confirmTotpSetup(userId, code);
        
        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }

    /**
     * Verifica un código TOTP durante el login
     */
    @PostMapping("/totp/verify")
    public ResponseEntity<Map<String, String>> verifyTotpCode(
            @RequestHeader("Authorization") String authHeader, 
            @Valid @RequestBody CodeVerificationDTO payload) {
        
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();
        
        boolean isValid = twoFactorAuthService.verifyTotpCode(userId, code);
        
        if (isValid) {
            logger.info("Código TOTP verificado exitosamente para usuario {}", userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, TOTP_CODE_VERIFIED_SUCCESSFULLY));
        }
        else {
            logger.warn("Código TOTP inválido para usuario {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, TOTP_CODE_INVALID);
        }
    }

    /**
     * Desactiva 2FA para el usuario
     */
    @PostMapping("/disable")
    public ResponseEntity<Map<String, String>> disableTwoFactor(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        
        logger.info("Usuario {} desactivando 2FA", userId);
        String message = twoFactorAuthService.disableTwoFactor(userId);
        
        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }

    /**
     * Desactiva el tercer factor (TOTP) para el usuario
     */
    @PostMapping("/totp/disable")
    public ResponseEntity<Map<String, String>> disableThirdFactor(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        
        logger.info("Usuario {} desactivando tercer factor (TOTP)", userId);
        String message = twoFactorAuthService.disableThirdFactor(userId);
        
        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }
    

    // ===== ENDPOINTS PARA ADMINISTRADORES =====

    /**
     * Envía un código de verificación por email al administrador autenticado
     */
    @PostMapping("/admin/email/send")
    public ResponseEntity<Map<String, String>> sendEmailCodeAdmin(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);

        logger.info("Administrador {} solicitó código por email", userId);
        String message = twoFactorAuthService.sendEmailVerificationCode(userId);

        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }

    /**
     * Verifica un código enviado por email para administrador
     */
    @PostMapping("/admin/email/verify")
    public ResponseEntity<Map<String, String>> verifyEmailCodeAdmin(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();

        boolean isValid = twoFactorAuthService.verifyEmailCodeAdmin(userId, code);

        if (isValid) {
            logger.info("Código de email verificado exitosamente para administrador {}", userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, CODE_VERIFIED_SUCCESSFULLY));
        }
        else
        {
            logger.warn("Código de email inválido para administrador {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CODE_INVALID_OR_EXPIRED);
        }
    }

    /**
     * Configura TOTP para el administrador - Genera QR
     */
    @PostMapping("/admin/totp/setup")
    public ResponseEntity<Map<String, Object>> setupTotpAdmin(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);

        logger.info("Administrador {} iniciando configuración TOTP", userId);
        TotpSetupResponse response = twoFactorAuthService.setupTotpAdmin(userId);

        return ResponseEntity.ok(Map.of(
            QR_CODE_URL_KEY, response.getQrCodeUrl(),
            SECRET_KEY, response.getSecretKey(),
            MESSAGE_KEY, QR_SETUP_MESSAGE
        ));
    }

    /**
     * Confirma la configuración TOTP con el primer código para administrador
     */
    @PostMapping("/admin/totp/confirm")
    public ResponseEntity<Map<String, String>> confirmTotpSetupAdmin(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();

        logger.info("Administrador {} confirmando configuración TOTP", userId);
        String message = twoFactorAuthService.confirmTotpSetupAdmin(userId, code);

        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }

    /**
     * Verifica un código TOTP para administrador durante el login
     */
    @PostMapping("/admin/totp/verify")
    public ResponseEntity<Map<String, String>> verifyTotpCodeAdmin(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();

        boolean isValid = twoFactorAuthService.verifyTotpCodeAdmin(userId, code);

        if (isValid) {
            logger.info("Código TOTP verificado exitosamente para administrador {}", userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, TOTP_CODE_VERIFIED_SUCCESSFULLY));
        } 
        else {
            logger.warn("Código TOTP inválido para administrador {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, TOTP_CODE_INVALID);
        }
    }

    // ===== ENDPOINTS PARA CREADORES DE CONTENIDO =====

    /**
     * Envía un código de verificación por email al creador de contenido autenticado
     */
    @PostMapping("/creator/email/send")
    public ResponseEntity<Map<String, String>> sendEmailCodeCreator(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);

        logger.info("Creador {} solicitó código por email", userId);
        String message = twoFactorAuthService.sendEmailVerificationCode(userId);

        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }

    /**
     * Verifica un código enviado por email para creador de contenido
     */
    @PostMapping("/creator/email/verify")
    public ResponseEntity<Map<String, String>> verifyEmailCodeCreator(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();

        boolean isValid = twoFactorAuthService.verifyEmailCodeCreador(userId, code);

        if (isValid) {
            logger.info("Código de email verificado exitosamente para creador {}", userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, CODE_VERIFIED_SUCCESSFULLY));
        }
        else
        {
            logger.warn("Código de email inválido para creador {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CODE_INVALID_OR_EXPIRED);
        }
    }

    /**
     * Configura TOTP para el creador de contenido - Genera QR
     */
    @PostMapping("/creator/totp/setup")
    public ResponseEntity<Map<String, Object>> setupTotpCreator(@RequestHeader("Authorization") String authHeader) {
        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);

        logger.info("Creador {} iniciando configuración TOTP", userId);
        TotpSetupResponse response = twoFactorAuthService.setupTotpCreador(userId);

        return ResponseEntity.ok(Map.of(
            QR_CODE_URL_KEY, response.getQrCodeUrl(),
            SECRET_KEY, response.getSecretKey(),
            MESSAGE_KEY, QR_SETUP_MESSAGE
        ));
    }

    /**
     * Confirma la configuración TOTP con el primer código para creador de contenido
     */
    @PostMapping("/creator/totp/confirm")
    public ResponseEntity<Map<String, String>> confirmTotpSetupCreator(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();

        logger.info("Creador {} confirmando configuración TOTP", userId);
        String message = twoFactorAuthService.confirmTotpSetupCreador(userId, code);

        return ResponseEntity.ok(Map.of(MESSAGE_KEY, message));
    }

    /**
     * Verifica un código TOTP para creador de contenido durante el login
     */
    @PostMapping("/creator/totp/verify")
    public ResponseEntity<Map<String, String>> verifyTotpCodeCreator(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CodeVerificationDTO payload) {

        String userId = twoFactorAuthService.validarEntradaPara2O3FA(authHeader);
        String code = payload.getCode();

        boolean isValid = twoFactorAuthService.verifyTotpCodeCreador(userId, code);

        if (isValid) {
            logger.info("Código TOTP verificado exitosamente para creador {}", userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, TOTP_CODE_VERIFIED_SUCCESSFULLY));
        } 
        else {
            logger.warn("Código TOTP inválido para creador {}", userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, TOTP_CODE_INVALID);
        }
    }

    
}