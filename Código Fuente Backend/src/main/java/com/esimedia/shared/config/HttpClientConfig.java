package com.esimedia.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuración de cliente HTTP para llamadas a APIs externas.
 * 
 * Proporciona:
 * - RestTemplate para llamadas HTTP a Leak-Lookup y otros servicios
 * - ObjectMapper para serialización/deserialización JSON
 */
@Configuration
public class HttpClientConfig {
    
    /**
     * Configura RestTemplate con timeouts y propiedades por defecto.
     * Se usa para llamadas a APIs externas como Leak-Lookup.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    /**
     * Configura ObjectMapper para serialización JSON.
     * Se usa para parsear respuestas de APIs externas.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

