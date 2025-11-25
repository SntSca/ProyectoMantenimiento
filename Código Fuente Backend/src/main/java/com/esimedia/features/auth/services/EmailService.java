package com.esimedia.features.auth.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.entity.Usuario;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // Constante para literales duplicados
    private static final String HTML_CONTENT_TYPE = "text/html; charset=utf-8";
    private static final String USERNAME_PLACEHOLDER = "{{username}}";

    @Value("${email.remitente}")
    private String remitente;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${email.confirmationUrl}")
    private String confirmationUrl;

    /**
     * Envía un correo de confirmación de cuenta
     */
    public void sendConfirmationEmail(Usuario usuario, String token, String expirationTime) {
        try {
            String content = loadEmailTemplate("welcome.html.txt");
            content = content.replace(USERNAME_PLACEHOLDER, usuario.getAlias());
            content = content.replace("{{confirmationLink}}", confirmationUrl + "/" + token);
            content = content.replace("{{expirationTime}}", expirationTime);

            sendEmailViaSendGrid(usuario.getEmail(), "Confirma tu cuenta en nuestra plataforma", content);
            logger.info("[EMAIL] Correo de confirmación enviado a: {}", usuario.getEmail());
        } 
        catch (Exception e) {
            logger.error("[EMAIL] Error enviando correo de confirmación a {}: {}", usuario.getEmail(), e.getMessage(), e);
            throw new IllegalStateException("No se pudo enviar el correo de confirmación", e);
        }
    }

    public void resetEmail(String to, String subject, String resetLink, String expirationTime) {
        try {
            String content = loadEmailTemplate("reset-password.html.txt");
            content = content.replace("{{resetLink}}", resetLink);
            content = content.replace("{{expirationTime}}", expirationTime);

            sendEmailViaSendGrid(to, subject, content);
            logger.info("[EMAIL] Correo de reset de contraseña enviado a: {}", to);
        } 
        catch (Exception e) {
            logger.error("[EMAIL] Error enviando correo de reset a {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("No se pudo enviar el correo de reset de contraseña", e);
        }
    }
    
    private String loadEmailTemplate(String filename) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(filename).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } 
        catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar la plantilla del correo: " + filename, e);
        }
    }

    /**
     * Envía un correo de alerta de seguridad
     */
    public void sendSecurityAlertEmail(Usuario user, int attempts) {
        try {
            String content = loadEmailTemplate("security-alert.html.txt");
            content = content.replace(USERNAME_PLACEHOLDER, user.getEmail());
            content = content.replace("{{attempts}}", String.valueOf(attempts));

            sendEmailViaSendGrid(user.getEmail(), "Alerta de seguridad: intentos fallidos de acceso", content);
            logger.info("[EMAIL] Correo de alerta de seguridad enviado a: {}", user.getEmail());
        } 
        catch (Exception e) {
            logger.error("[EMAIL] Error enviando correo de alerta a {}: {}", user.getEmail(), e.getMessage(), e);
            throw new IllegalStateException("No se pudo enviar el correo de alerta de seguridad", e);
        }
    }

    /**
     * Envía un código de verificación de dos factores por email
     */
    public void sendTwoFactorCode(Usuario usuario, String verificationCode, int expirationMinutes) {
        try {
            String content = loadEmailTemplate("two-factor-code.html.txt");
            content = content.replace(USERNAME_PLACEHOLDER, usuario.getAlias());
            content = content.replace("{{verificationCode}}", verificationCode);
            content = content.replace("{{expirationMinutes}}", String.valueOf(expirationMinutes));

            sendEmailViaSendGrid(usuario.getEmail(), "Codigo de verificacion - EsiMedia", content);
            logger.info("[EMAIL] Correo de código 2FA enviado a: {}", usuario.getEmail());
        } 
        catch (Exception e) {
            logger.error("[EMAIL] Error enviando correo de 2FA a {}: {}", usuario.getEmail(), e.getMessage(), e);
            throw new IllegalStateException("No se pudo enviar el correo de código de verificación", e);
        }
    }

    /**
     * Método privado para enviar correos usando SendGrid API con JSON directo
     */
    private void sendEmailViaSendGrid(String destinatario, String asunto, String contenidoHtml) throws Exception {
        SendGrid sg = new SendGrid(sendGridApiKey);
        
        // Construir JSON manualmente
        String json = "{"
            + "\"personalizations\":[{\"to\":[{\"email\":\"" + destinatario + "\"}]}],"
            + "\"from\":{\"email\":\"" + remitente + "\"},"
            + "\"subject\":\"" + asunto.replace("\"", "\\\"") + "\","
            + "\"content\":[{\"type\":\"text/html\",\"value\":\"" 
            + contenidoHtml.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]"
            + "}";

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(json);

        Response response = sg.api(request);

        // Validar respuesta de SendGrid
        if (response.getStatusCode() >= 400) {
            logger.error("[EMAIL] Error de SendGrid - Status: {}, Body: {}", 
                response.getStatusCode(), response.getBody());
            throw new IllegalStateException("Error enviando correo con SendGrid: " + response.getBody());
        }

        logger.debug("[EMAIL] Correo enviado exitosamente - Status: {}", response.getStatusCode());
    }
}
