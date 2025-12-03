package com.esimedia.shared.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.services.SesionService;
import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.shared.util.JwtValidationUtil;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger jwtLogger = LoggerFactory.getLogger(JwtFilter.class);

    private static final List<String> EXACT_STATIC_PATHS = Arrays.asList("/", "");
    private static final List<String> PREFIX_STATIC_PATHS = Arrays.asList("/favicon.ico", "/static/", "/assets/", "/css/", "/js/", "/images/");

    private final SesionService sesionService;
    private final JwtValidationUtil jwtValidationService;
    private final UserRetrievalService userRetrievalService;
    
    public JwtFilter(SesionService sesionService, JwtValidationUtil jwtValidationService, UserRetrievalService userRetrievalService) {
        this.sesionService = sesionService;
        this.jwtValidationService = jwtValidationService;
        this.userRetrievalService = userRetrievalService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        jwtLogger.info("[DEBUG] JwtFilter evaluando path: {}", path);
        boolean excluded = false;

        // Excluir raíz y rutas estáticas
        if (EXACT_STATIC_PATHS.contains(path) || PREFIX_STATIC_PATHS.stream().anyMatch(path::startsWith)) {
            excluded = true;
        }
        
        // Excluir rutas públicas del filtro JWT
        if (path.equals("/users/register/standard") || path.equals("/users/register/creator")) {
            excluded = true;
        }
        if (path.startsWith("/users/register/confirm/")) {
            excluded = true;
        }
        if (path.equals("/users/password/forgot") || path.equals("/users/password/reset") || path.equals("/users/password/forgot-privileged")) {
            excluded = true;
        }
        if (path.equals("/users/auth/privileged-login") || path.equals("/users/auth/login")) {
            excluded = true;
        }
        if (path.equals("/users/auth/step1") || path.equals("/users/auth/step2") || path.equals("/users/auth/step3")) {
            excluded = true;
        }
        if (path.startsWith("/users/password/validate-reset-token/") || path.startsWith("/users/confirm/")) {
            excluded = true;
        }
        if (path.startsWith("/content/")) {
            excluded = true;
        }

        jwtLogger.info("[DEBUG] shouldNotFilter para {}: {}", path, excluded);
        return excluded;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        

        jwtLogger.debug("\n\n[DEBUG] Incoming {} {}\n\n", request.getMethod(), request.getRequestURI());
        String requestPath = request.getRequestURI();
        jwtLogger.debug("[DEBUG] JwtFilter procesando ruta protegida: {}", requestPath);
        
        
        
        // Extraer y validar token una sola vez
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            jwtLogger.warn("Protected endpoint accessed without token: {}", requestPath);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token requerido");
        }

        
        try {
            jwtValidationService.validarGenerico(authHeader);
            String userId = jwtValidationService.validarGenerico(authHeader);
            Usuario user = userRetrievalService.findAnyUserById(userId).orElse(null);
            if (user == null || user.getRol() == null) {
                jwtLogger.warn("Usuario no encontrado para ID: {}", userId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no encontrado");
            }


            jwtLogger.info("[DEBUG] Petición aceptada para la ruta: {}", requestPath);

        } 
        catch (JwtException e) {
            jwtLogger.error("Invalid or expired token for path: {}", requestPath, e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
        } 
        catch (Exception e) {
            jwtLogger.error("Error inesperado en doFilterInternal para path: {}", requestPath, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno");
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene la IP real del cliente considerando proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Tomar la primera IP en caso de múltiples
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
