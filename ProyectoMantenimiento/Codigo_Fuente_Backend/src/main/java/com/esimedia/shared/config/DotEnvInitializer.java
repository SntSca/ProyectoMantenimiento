package com.esimedia.shared.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static final Logger logger = LoggerFactory.getLogger(DotEnvInitializer.class);
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        try {
            // Busca el archivo .env en el directorio raíz del proyecto
            File envFile = new File(".env");
            if (envFile.exists()) {
                Dotenv dotenv = Dotenv.configure()
                    .filename(".env")
                    .load();
                
                Map<String, Object> envVars = new HashMap<>();
                dotenv.entries().forEach(entry -> 
                    envVars.put(entry.getKey(), entry.getValue()));
                
                MapPropertySource envPropertySource = new MapPropertySource("dotenv", envVars);
                environment.getPropertySources().addFirst(envPropertySource);
                
                logger.info("Archivo .env cargado exitosamente con {} variables", envVars.size());
            } 
            else {
                logger.warn("Archivo .env no encontrado en el directorio raíz del proyecto");
            }
        } 
        catch (Exception e) {
            logger.error("Error al cargar el archivo .env: {}", e.getMessage());
        }
    }
}