package com.esimedia.shared.config;

import com.esimedia.features.auth.services.SesionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextClosedEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionShutdownHandlerTest {

    @Mock
    private SesionService sesionService;

    @InjectMocks
    private SesionShutdownHandler sesionShutdownHandler;

    @Test
    @DisplayName("Debe crear instancia de SesionShutdownHandler con SesionService")
    void testConstructor() {
        // Given
        SesionService mockService = mock(SesionService.class);

        // When
        SesionShutdownHandler handler = new SesionShutdownHandler(mockService);

        // Then
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Debe expirar todas las sesiones activas al cerrar la aplicación")
    void testOnShutdownExpiraSesiones() {
        // When
        sesionShutdownHandler.onShutdown();

        // Then
        verify(sesionService, times(1)).expirarTodasSesionesActivas();
    }

    @Test
    @DisplayName("Debe llamar al servicio exactamente una vez")
    void testOnShutdownLlamadaUnica() {
        // When
        sesionShutdownHandler.onShutdown();

        // Then
        verify(sesionService, times(1)).expirarTodasSesionesActivas();
        verifyNoMoreInteractions(sesionService);
    }

    @Test
    @DisplayName("Debe manejar excepciones del servicio correctamente")
    void testOnShutdownCuandoServicioLanzaExcepcion() {
        // Given
        doThrow(new RuntimeException("Error al expirar sesiones")).when(sesionService).expirarTodasSesionesActivas();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            sesionShutdownHandler.onShutdown();
        });

        verify(sesionService, times(1)).expirarTodasSesionesActivas();
    }

    @Test
    @DisplayName("Debe tener anotación EventListener para ContextClosedEvent")
    void testEventListenerAnnotation() throws NoSuchMethodException {
        // When
        var method = SesionShutdownHandler.class.getMethod("onShutdown");
        var annotation = method.getAnnotation(org.springframework.context.event.EventListener.class);

        // Then
        assertNotNull(annotation);
        assertEquals(1, annotation.value().length);
        assertEquals(ContextClosedEvent.class, annotation.value()[0]);
    }

    @Test
    @DisplayName("Debe poder ejecutarse múltiples veces sin errores")
    void testOnShutdownMultiplesEjecuciones() {
        // When
        sesionShutdownHandler.onShutdown();
        sesionShutdownHandler.onShutdown();
        sesionShutdownHandler.onShutdown();

        // Then
        verify(sesionService, times(3)).expirarTodasSesionesActivas();
    }

    @Test
    @DisplayName("Debe tener un logger configurado")
    void testLoggerExiste() throws NoSuchFieldException {
        // When
        java.lang.reflect.Field loggerField = SesionShutdownHandler.class.getDeclaredField("logger");
        loggerField.setAccessible(true);

        // Then
        assertNotNull(loggerField);
        assertEquals(org.slf4j.Logger.class, loggerField.getType());
    }

    @Test
    @DisplayName("Debe ser un componente de Spring")
    void testComponentAnnotation() {
        // When
        var annotation = SesionShutdownHandler.class.getAnnotation(org.springframework.stereotype.Component.class);

        // Then
        assertNotNull(annotation);
    }

    @Test
    @DisplayName("Debe continuar funcionando si el servicio no hace nada")
    void testOnShutdownCuandoServicioNoHaceNada() {
        // Given
        doNothing().when(sesionService).expirarTodasSesionesActivas();

        // When
        assertDoesNotThrow(() -> sesionShutdownHandler.onShutdown());

        // Then
        verify(sesionService, times(1)).expirarTodasSesionesActivas();
    }
}