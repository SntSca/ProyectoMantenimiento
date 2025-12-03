package com.esimedia.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.esimedia.features.auth.services.SesionService;

import org.springframework.context.event.ContextClosedEvent;

@Component
public class SesionShutdownHandler {

    private static final Logger logger = LoggerFactory.getLogger(SesionShutdownHandler.class);
    private final SesionService sesionService;

    public SesionShutdownHandler(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        logger.info("Aplicación cerrándose: expirando todas las sesiones activas...");
        sesionService.expirarTodasSesionesActivas();
        logger.info("Sesiones expiradas al cerrar la app.");
    }
}