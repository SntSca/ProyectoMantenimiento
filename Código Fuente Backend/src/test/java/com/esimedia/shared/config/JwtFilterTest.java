package com.esimedia.shared.config;

import com.esimedia.features.auth.services.SesionService;
import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.shared.util.JwtValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private SesionService sesionService;

    @Mock
    private JwtValidationUtil jwtValidationService;

    @Mock
    private UserRetrievalService userRetrievalService;

    @Mock
    private HttpServletRequest request;

    private JwtFilter jwtFilter;

    @Test
    @DisplayName("shouldNotFilter debe excluir rutas estáticas")
    void testShouldNotFilterStaticPaths() {
        jwtFilter = new TestableJwtFilter(sesionService, jwtValidationService, userRetrievalService);

        // Test root
        when(request.getRequestURI()).thenReturn("/");
        assertTrue(jwtFilter.shouldNotFilter(request));

        // Test empty
        when(request.getRequestURI()).thenReturn("");
        assertTrue(jwtFilter.shouldNotFilter(request));

        // Test favicon
        when(request.getRequestURI()).thenReturn("/favicon.ico");
        assertTrue(jwtFilter.shouldNotFilter(request));

        // Test static
        when(request.getRequestURI()).thenReturn("/static/app.js");
        assertTrue(jwtFilter.shouldNotFilter(request));

        // Test assets
        when(request.getRequestURI()).thenReturn("/assets/image.png");
        assertTrue(jwtFilter.shouldNotFilter(request));

        // Test css
        when(request.getRequestURI()).thenReturn("/css/style.css");
        assertTrue(jwtFilter.shouldNotFilter(request));

        // Test js
        when(request.getRequestURI()).thenReturn("/js/script.js");
        assertTrue(jwtFilter.shouldNotFilter(request));

        // Test images
        when(request.getRequestURI()).thenReturn("/images/logo.png");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter debe excluir rutas públicas de registro")
    void testShouldNotFilterPublicRegistrationPaths() {
        jwtFilter = new TestableJwtFilter(sesionService, jwtValidationService, userRetrievalService);

        when(request.getRequestURI()).thenReturn("/users/register/standard");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/register/creator");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/register/confirm/some-token");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter debe excluir rutas públicas de password")
    void testShouldNotFilterPublicPasswordPaths() {
        jwtFilter = new TestableJwtFilter(sesionService, jwtValidationService, userRetrievalService);

        when(request.getRequestURI()).thenReturn("/users/password/forgot");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/password/reset");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/password/forgot-privileged");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/password/validate-reset-token/abc");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter debe excluir rutas de login")
    void testShouldNotFilterLoginPaths() {
        jwtFilter = new TestableJwtFilter(sesionService, jwtValidationService, userRetrievalService);

        when(request.getRequestURI()).thenReturn("/users/auth/privileged-login");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/auth/login");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/auth/step1");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/auth/step2");
        assertTrue(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/users/auth/step3");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter debe excluir rutas de confirmación")
    void testShouldNotFilterConfirmPaths() {
        jwtFilter = new TestableJwtFilter(sesionService, jwtValidationService, userRetrievalService);

        when(request.getRequestURI()).thenReturn("/users/confirm/xyz");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter debe excluir rutas de contenido")
    void testShouldNotFilterContentPaths() {
        jwtFilter = new TestableJwtFilter(sesionService, jwtValidationService, userRetrievalService);

        when(request.getRequestURI()).thenReturn("/content/video.mp4");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter debe filtrar rutas protegidas")
    void testShouldNotFilterProtectedPaths() {
        jwtFilter = new TestableJwtFilter(sesionService, jwtValidationService, userRetrievalService);

        when(request.getRequestURI()).thenReturn("/users/profile");
        assertFalse(jwtFilter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/admin/dashboard");
        assertFalse(jwtFilter.shouldNotFilter(request));
    }

    // Inner class to expose shouldNotFilter
    private static class TestableJwtFilter extends JwtFilter {
        public TestableJwtFilter(SesionService sesionService, JwtValidationUtil jwtValidationService, UserRetrievalService userRetrievalService) {
            super(sesionService, jwtValidationService, userRetrievalService);
        }

        @Override
        public boolean shouldNotFilter(HttpServletRequest request) {
            return super.shouldNotFilter(request);
        }
    }
}