package com.esimedia.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;



@Configuration
public class SecurityConfig {
    
    private final JwtFilter jwtFilter;
    private final SessionTimeoutFilter sessionTimeoutFilter;
    
    public SecurityConfig(JwtFilter jwtFilter, SessionTimeoutFilter sessionTimeoutFilter) {
        this.jwtFilter = jwtFilter;
        this.sessionTimeoutFilter = sessionTimeoutFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http
            .csrf(csrf -> csrf.disable())
            // Usa el CorsConfigurationSource definido en el contexto
            .cors().and() 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/users/register-standard",
                    "/users/register-creator",
                    "/users/forgot-password",
                    "/users/reset-password",
                    "/users/validate-reset-token/**",
                    "/users/confirm/**",
                    "/users/privileged-login",
                    "/users/login",
                    "/users/logout",
                    "/users/forgot-password-privileged",
                    "/content/**",
                    "/management/**"
                ).permitAll()
                // Cambiar a permitAll para validaciones manuales
                .anyRequest().permitAll() 
            )
            // JwtFilter primero para validar el token
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // SessionTimeoutFilter después para validar el absolute timeout
            .addFilterAfter(sessionTimeoutFilter, JwtFilter.class)
            .sessionManagement(session -> session
                .maximumSessions(3)
                .maxSessionsPreventsLogin(true)
            )
            .formLogin(login -> login.disable())
            .httpBasic(basic -> basic.disable());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // URL del frontend desplegado en Firebase
        config.setAllowedOrigins(List.of("https://esimedia-frontend-2fb.web.app" ,
                                        "http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}