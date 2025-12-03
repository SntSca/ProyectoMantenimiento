package com.esimedia.features.auth.services;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.esimedia.features.auth.dto.TotpSetupResponse;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TotpTwoFactorService {

    private static final String APP_NAME = "EsiMedia";
    private static final String TOTP_ACTIVATION_SUCCESS_MESSAGE = "3FA activado exitosamente. Guarda tus códigos de respaldo en un lugar seguro.";
    private static final int BACKUP_CODES_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;

    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    private static final String NO_CONFIG_TOTP_PENDIENTE = "No hay configuración TOTP pendiente";
    private static final String CODIGO_TOTP_INVALIDO = "Código TOTP inválido";

    private final UsuarioNormalRepository usuarioNormalRepository;
    private final AdminRepository adminRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;

    private final GoogleAuthenticator googleAuthenticator;
    private final SecureRandom secureRandom;

    public TotpTwoFactorService(UsuarioNormalRepository usuarioNormalRepository,
                                AdminRepository adminRepository,
                                CreadorContenidoRepository creadorContenidoRepository) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.adminRepository = adminRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.googleAuthenticator = new GoogleAuthenticator();
        this.secureRandom = new SecureRandom();
    }

    // For UsuarioNormal

    public TotpSetupResponse setupTotp(String userId) {
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secretKey = key.getKey();

        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            APP_NAME, usuario.getEmail(), key
        );

        usuario.setTotpSecret(secretKey);
        usuarioNormalRepository.save(usuario);

        return new TotpSetupResponse(qrCodeUrl, secretKey);
    }

    public String confirmTotpSetup(String userId, String verificationCode) {
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USUARIO_NO_ENCONTRADO));

        if (usuario.getTotpSecret() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_CONFIG_TOTP_PENDIENTE);
        }

        boolean isValidCode = googleAuthenticator.authorize(usuario.getTotpSecret(), Integer.parseInt(verificationCode));

        if (!isValidCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CODIGO_TOTP_INVALIDO);
        }

        usuario.setThirdFactorEnabled(true);
        usuario.setBackupCodes(generateBackupCodes());
        usuarioNormalRepository.save(usuario);

        return TOTP_ACTIVATION_SUCCESS_MESSAGE;
    }

    public boolean verifyTotpCode(String userId, String code) {
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        if (!usuario.isTwoFactorEnabled() || usuario.getTotpSecret() == null) {
            return false;
        }

        try {
            int totpCode = Integer.parseInt(code);
            return googleAuthenticator.authorize(usuario.getTotpSecret(), totpCode);
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public String disableTwoFactor(String userId) {
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        usuario.setTwoFactorEnabled(false);
        usuario.setTotpSecret(null);
        usuario.setBackupCodes(null);
        usuarioNormalRepository.save(usuario);

        return "2FA desactivado exitosamente";
    }

    public String disableThirdFactor(String userId) {
        UsuarioNormal usuario = usuarioNormalRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        usuario.setThirdFactorEnabled(false);
        usuario.setTotpSecret(null);
        usuario.setBackupCodes(null);
        usuarioNormalRepository.save(usuario);

        return "3FA desactivado exitosamente";
    }

    // For Administrador

    public TotpSetupResponse setupTotpAdmin(String userId) {
        Administrador usuario = adminRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secretKey = key.getKey();

        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            APP_NAME, usuario.getEmail(), key
        );

        usuario.setTotpSecret(secretKey);
        adminRepository.save(usuario);

        return new TotpSetupResponse(qrCodeUrl, secretKey);
    }

    public String confirmTotpSetupAdmin(String userId, String verificationCode) {
        Administrador usuario = adminRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USUARIO_NO_ENCONTRADO));

        if (usuario.getTotpSecret() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_CONFIG_TOTP_PENDIENTE);
        }

        boolean isValidCode = googleAuthenticator.authorize(usuario.getTotpSecret(), Integer.parseInt(verificationCode));

        if (!isValidCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CODIGO_TOTP_INVALIDO);
        }

        usuario.setThirdFactorEnabled(true);
        usuario.setBackupCodes(generateBackupCodes());
        adminRepository.save(usuario);

        return TOTP_ACTIVATION_SUCCESS_MESSAGE;
    }

    public boolean verifyTotpCodeAdmin(String userId, String code) {
        Administrador usuario = adminRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        if (!usuario.isTwoFactorEnabled() || usuario.getTotpSecret() == null) {
            return false;
        }

        try {
            int totpCode = Integer.parseInt(code);
            return googleAuthenticator.authorize(usuario.getTotpSecret(), totpCode);
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    // For CreadorContenido

    public TotpSetupResponse setupTotpCreador(String userId) {
        CreadorContenido usuario = creadorContenidoRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secretKey = key.getKey();

        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            APP_NAME, usuario.getEmail(), key
        );

        usuario.setTotpSecret(secretKey);
        creadorContenidoRepository.save(usuario);

        return new TotpSetupResponse(qrCodeUrl, secretKey);
    }

    public String confirmTotpSetupCreador(String userId, String verificationCode) {
        CreadorContenido usuario = creadorContenidoRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USUARIO_NO_ENCONTRADO));

        if (usuario.getTotpSecret() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_CONFIG_TOTP_PENDIENTE);
        }

        boolean isValidCode = googleAuthenticator.authorize(usuario.getTotpSecret(), Integer.parseInt(verificationCode));

        if (!isValidCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CODIGO_TOTP_INVALIDO);
        }

        usuario.setThirdFactorEnabled(true);
        usuario.setBackupCodes(generateBackupCodes());
        creadorContenidoRepository.save(usuario);

        return TOTP_ACTIVATION_SUCCESS_MESSAGE;
    }

    public boolean verifyTotpCodeCreador(String userId, String code) {
        CreadorContenido usuario = creadorContenidoRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));

        if (!usuario.isTwoFactorEnabled() || usuario.getTotpSecret() == null) {
            return false;
        }

        try {
            int totpCode = Integer.parseInt(code);
            return googleAuthenticator.authorize(usuario.getTotpSecret(), totpCode);
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < BACKUP_CODES_COUNT; i++) {
            codes.add(generateAlphanumericCode(BACKUP_CODE_LENGTH));
        }
        return codes;
    }

    private String generateAlphanumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return code.toString();
    }
}