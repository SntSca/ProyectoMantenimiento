package com.esimedia.features.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import com.esimedia.features.auth.entity.Usuario;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        // Configurar propiedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "remitente", "test@example.com");
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "confirmationUrl", "https://example.com/confirm");

        // Crear usuario mock
        mockUsuario = new Usuario();
        mockUsuario.setEmail("usuario@test.com");
        mockUsuario.setAlias("TestUser");
    }

    @Test
    void testSendConfirmationEmail_Success() throws Exception {
        // Arrange
        String token = "test-token-123";
        String expirationTime = "24 horas";
        String templateContent = "Hola {{username}}, confirma tu cuenta: {{confirmationLink}}. Expira en {{expirationTime}}.";

        // Mock ClassPathResource y su InputStream
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(202);
                    mockResponse.setBody("Success");
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act
            emailService.sendConfirmationEmail(mockUsuario, token, expirationTime);

            // Assert
            assertEquals(1, mockedSendGrid.constructed().size());
            assertEquals(1, mockedResource.constructed().size());
        }
    }

    @Test
    void testSendConfirmationEmail_ThrowsException_WhenSendGridFails() throws Exception {
        // Arrange
        String token = "test-token-123";
        String expirationTime = "24 horas";
        String templateContent = "Hola {{username}}, confirma: {{confirmationLink}}. Expira: {{expirationTime}}.";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(400);
                    mockResponse.setBody("Error from SendGrid");
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                emailService.sendConfirmationEmail(mockUsuario, token, expirationTime);
            });

            assertTrue(exception.getMessage().contains("No se pudo enviar el correo de confirmación"));
        }
    }

    @Test
    void testResetEmail_Success() throws Exception {
        // Arrange
        String to = "usuario@test.com";
        String subject = "Restablecer contraseña";
        String resetLink = "https://example.com/reset/token123";
        String expirationTime = "1 hora";
        String templateContent = "Restablecer: {{resetLink}}. Expira en {{expirationTime}}.";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(202);
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act
            emailService.resetEmail(to, subject, resetLink, expirationTime);

            // Assert
            assertEquals(1, mockedSendGrid.constructed().size());
        }
    }

    @Test
    void testResetEmail_ThrowsException_WhenTemplateLoadFails() {
        // Arrange
        String to = "usuario@test.com";
        String subject = "Restablecer contraseña";
        String resetLink = "https://example.com/reset/token123";
        String expirationTime = "1 hora";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream()).thenThrow(new RuntimeException("Template not found"));
                })) {

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                emailService.resetEmail(to, subject, resetLink, expirationTime);
            });

            assertTrue(exception.getMessage().contains("No se pudo enviar el correo de reset de contraseña"));
        }
    }

    @Test
    void testSendSecurityAlertEmail_Success() throws Exception {
        // Arrange
        int attempts = 5;
        String templateContent = "Alerta de seguridad para {{username}}. Intentos: {{attempts}}.";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(202);
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act
            emailService.sendSecurityAlertEmail(mockUsuario, attempts);

            // Assert
            assertEquals(1, mockedSendGrid.constructed().size());
        }
    }

    @Test
    void testSendSecurityAlertEmail_ThrowsException() throws Exception {
        // Arrange
        int attempts = 3;
        String templateContent = "Alerta: {{username}}, intentos: {{attempts}}.";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(500);
                    mockResponse.setBody("Server error");
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                emailService.sendSecurityAlertEmail(mockUsuario, attempts);
            });

            assertTrue(exception.getMessage().contains("No se pudo enviar el correo de alerta de seguridad"));
        }
    }

    @Test
    void testSendTwoFactorCode_Success() throws Exception {
        // Arrange
        String verificationCode = "123456";
        int expirationMinutes = 10;
        String templateContent = "Hola {{username}}, tu código: {{verificationCode}}. Expira en {{expirationMinutes}} minutos.";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(202);
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act
            emailService.sendTwoFactorCode(mockUsuario, verificationCode, expirationMinutes);

            // Assert
            assertEquals(1, mockedSendGrid.constructed().size());
        }
    }

    @Test
    void testSendTwoFactorCode_ThrowsException() throws Exception {
        // Arrange
        String verificationCode = "654321";
        int expirationMinutes = 5;
        String templateContent = "Código: {{verificationCode}} para {{username}}.";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(401);
                    mockResponse.setBody("Unauthorized");
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                emailService.sendTwoFactorCode(mockUsuario, verificationCode, expirationMinutes);
            });

            assertTrue(exception.getMessage().contains("No se pudo enviar el correo de código de verificación"));
        }
    }

    @Test
    void testSendEmailViaSendGrid_WithSpecialCharacters() throws Exception {
        // Arrange
        String templateContent = "Contenido con \"comillas\" y \nsaltos de línea.";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(202);
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act
            emailService.resetEmail("test@test.com", "Test \"Subject\"", "http://link.com", "1h");

            // Assert - Verificar que se manejaron los caracteres especiales
            assertEquals(1, mockedSendGrid.constructed().size());
        }
    }

    @Test
    void testLoadEmailTemplate_ThrowsException_WhenFileNotFound() {
        // Este test verifica el comportamiento cuando la plantilla no existe
        // Ya está cubierto indirectamente por testResetEmail_ThrowsException_WhenTemplateLoadFails
        // pero lo incluyo explícitamente para mayor claridad
        
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream()).thenThrow(new RuntimeException("File not found"));
                })) {

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                emailService.sendConfirmationEmail(mockUsuario, "token", "24h");
            });

            assertNotNull(exception.getMessage());
        }
    }

    @Test
    void testSendEmailViaSendGrid_DifferentStatusCodes() throws Exception {
        // Test con código 200 (también exitoso)
        String templateContent = "Test content";

        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    InputStream is = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
                    when(mock.getInputStream()).thenReturn(is);
                });
             MockedConstruction<SendGrid> mockedSendGrid = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(200);
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act - no debería lanzar excepción
            emailService.resetEmail("test@test.com", "Subject", "http://link.com", "1h");

            // Assert
            assertEquals(1, mockedSendGrid.constructed().size());
        }
    }
}