package com.esimedia.shared.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.esimedia.features.auth.services.SessionTimeoutService;
import com.esimedia.shared.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro para validar el absolute timeout de las sesiones.
 * Se ejecuta después de JwtFilter para validar que la sesión no haya excedido el tiempo límite.
 */
@Component
public class SessionTimeoutFilter extends OncePerRequestFilter {
    
    private static final Logger loggerF = LoggerFactory.getLogger(SessionTimeoutFilter.class);
    
    private final SessionTimeoutService sessionTimeoutService;
    private final JwtUtil jwtUtil;
    
    public SessionTimeoutFilter(SessionTimeoutService sessionTimeoutService, JwtUtil jwtUtil) {
        this.sessionTimeoutService = sessionTimeoutService;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        // Si no hay token, continuar (el JwtFilter se encargará de eso)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extraer userId del token
            String userId = jwtUtil.getUserIdFromToken(authHeader);
            
            // Validar si la sesión sigue siendo válida (no ha expirado el absolute timeout)
            if (!sessionTimeoutService.isSessionValid(userId)) {
                loggerF.warn("[SESSION-TIMEOUT] Sesión expirada por absolute timeout para usuario: {}", userId);
                
                // Invalidar la sesión
                sessionTimeoutService.invalidateSession(userId);
                
                // Responder con 401 Unauthorized
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Sesión expirada por timeout absoluto. Por favor, vuelva a iniciar sesión.\"}");
                return;
            }
            
            // Si la sesión es válida, continuar con la cadena de filtros
            filterChain.doFilter(request, response);
            
        } 
        catch (Exception e) {
            loggerF.error("[SESSION-TIMEOUT] Error al validar absolute timeout", e);
            // Si hay error, continuar con la cadena (el JwtFilter manejará tokens inválidos)
            filterChain.doFilter(request, response);
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // No filtrar rutas de autenticación
        return path.startsWith("/users/auth/login") || 
               path.startsWith("/users/auth/register") ||
               path.startsWith("/users/auth/privileged-login") ||
               path.startsWith("/users/auth/login-step");
    }
}
