package com.esimedia.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.esimedia.features.auth.services.SesionService;

@Component
public class SesionCleanup implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SesionCleanup.class);
    private final SesionService sesionService;

    public SesionCleanup(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @Override
    public void run(String... args) {
        logger.info("Limpiando sesiones expiradas e inválidas al inicio...");
        sesionService.limpiarSesionesExpiradasEInvalidas();
        logger.info("Limpieza completada al iniciar la app.");
    }
}