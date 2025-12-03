package com.esimedia.features.auth.services;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import com.esimedia.features.auth.enums.TipoVerificacion;
import com.esimedia.features.auth.entity.CodigoVerificacion;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.repository.CodigoVerificacionRepository;
import com.esimedia.features.user_management.services.UserRetrievalService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.mail.MessagingException;

@Service
public class EmailTwoFactorService {

    private static final int EMAIL_CODE_LENGTH = 6;
    private static final int EMAIL_CODE_EXPIRATION_MINUTES = 10;

    private final EmailService emailService;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final UserRetrievalService userRetrievalService;

    public EmailTwoFactorService(EmailService emailService,
                                 CodigoVerificacionRepository codigoVerificacionRepository,
                                 UserRetrievalService userRetrievalService) {
        this.emailService = emailService;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.userRetrievalService = userRetrievalService;
    }

    public String sendEmailVerificationCode(String userId) {
        try {
            Usuario usuario = userRetrievalService.findAnyUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            invalidateExistingCodes(userId, TipoVerificacion.EMAIL_2FA);

            String codigo = generateNumericCode(EMAIL_CODE_LENGTH);

            CodigoVerificacion codigoVerificacion = new CodigoVerificacion(
                userId, codigo, TipoVerificacion.EMAIL_2FA, EMAIL_CODE_EXPIRATION_MINUTES
            );
            codigoVerificacionRepository.save(codigoVerificacion);

            sendEmailCode(usuario, codigo);

            
            return "Código de verificación +" + codigo + " enviado a " + maskEmail(usuario.getEmail());
            
        } 
        catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al enviar el email");
        }
    }
    
    public boolean verifyEmailCode(String userId, String codigo) {
        Optional<CodigoVerificacion> codigoOpt = codigoVerificacionRepository
            .findByUserIdAndCodigoAndTipo(userId, codigo, TipoVerificacion.EMAIL_2FA);

        if (!codigoOpt.isPresent()) {
            return false;
        }

        CodigoVerificacion codigoVerificacion = codigoOpt.get();
        
        if (!codigoVerificacion.isValido()) {
            return false;
        }

        // Marcar como usado
        codigoVerificacion.setUsado(true);
        codigoVerificacionRepository.save(codigoVerificacion);
        return true;
    }

    private void invalidateExistingCodes(String userId, TipoVerificacion tipo) {
        List<CodigoVerificacion> existingCodes = codigoVerificacionRepository
            .findByUserIdAndTipoAndUsadoFalse(userId, tipo);
        
        for (CodigoVerificacion codigo : existingCodes) {
            codigo.setUsado(true);
            codigoVerificacionRepository.save(codigo);
        }
    }

    private String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(new SecureRandom().nextInt(10));
        }
        return code.toString();
    }

    private void sendEmailCode(Usuario usuario, String codigo) throws MessagingException {
        emailService.sendTwoFactorCode(usuario, codigo, EMAIL_CODE_EXPIRATION_MINUTES);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "email";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "**@" + domain;
        }
        
        return username.substring(0, 2) + "***@" + domain;
    }
}