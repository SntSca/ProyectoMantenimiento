package com.esimedia.shared.config;

import com.esimedia.features.auth.services.SesionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionCleanupTest {

    @Mock
    private SesionService sesionService;

    @InjectMocks
    private SesionCleanup sesionCleanup;

    @Test
    @DisplayName("Debe crear instancia de SesionCleanup con SesionService")
    void testConstructor() {
        // Given
        SesionService mockService = mock(SesionService.class);

        // When
        SesionCleanup cleanup = new SesionCleanup(mockService);

        // Then
        assertNotNull(cleanup);
    }

    @Test
    @DisplayName("Debe ejecutar limpieza de sesiones al iniciar la aplicación")
    void testRunLimpiaSesiones() throws Exception {
        // Given
        String[] args = new String[]{};

        // When
        sesionCleanup.run(args);

        // Then
        verify(sesionService, times(1)).limpiarSesionesExpiradasEInvalidas();
    }

    @Test
    @DisplayName("Debe ejecutar limpieza con argumentos de línea de comandos")
    void testRunConArgumentos() throws Exception {
        // Given
        String[] args = new String[]{"arg1", "arg2", "arg3"};

        // When
        sesionCleanup.run(args);

        // Then
        verify(sesionService, times(1)).limpiarSesionesExpiradasEInvalidas();
    }

    @Test
    @DisplayName("Debe ejecutar limpieza sin argumentos")
    void testRunSinArgumentos() throws Exception {
        // When
        sesionCleanup.run();

        // Then
        verify(sesionService, times(1)).limpiarSesionesExpiradasEInvalidas();
    }

    @Test
    @DisplayName("Debe implementar CommandLineRunner")
    void testImplementaCommandLineRunner() {
        // Then
        assertInstanceOf(org.springframework.boot.CommandLineRunner.class, sesionCleanup);
    }

    @Test
    @DisplayName("Debe manejar excepciones del servicio correctamente")
    void testRunCuandoServicioLanzaExcepcion() {
        // Given
        doThrow(new RuntimeException("Error en limpieza")).when(sesionService).limpiarSesionesExpiradasEInvalidas();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            sesionCleanup.run();
        });

        verify(sesionService, times(1)).limpiarSesionesExpiradasEInvalidas();
    }

    @Test
    @DisplayName("Debe llamar al servicio solo una vez por ejecución")
    void testLlamadaUnicaAlServicio() throws Exception {
        // Given
        String[] args = new String[]{"test"};

        // When
        sesionCleanup.run(args);

        // Then
        verify(sesionService, times(1)).limpiarSesionesExpiradasEInvalidas();
        verifyNoMoreInteractions(sesionService);
    }

    @Test
    @DisplayName("Debe ejecutar limpieza incluso con array de argumentos null")
    void testRunConArgumentosNull() throws Exception {
        // Given
        String[] args = null;

        // When
        sesionCleanup.run(args);

        // Then
        verify(sesionService, times(1)).limpiarSesionesExpiradasEInvalidas();
    }

    @Test
    @DisplayName("Debe tener un logger configurado")
    void testLoggerExiste() throws NoSuchFieldException {
        // When
        java.lang.reflect.Field loggerField = SesionCleanup.class.getDeclaredField("logger");
        loggerField.setAccessible(true);

        // Then
        assertNotNull(loggerField);
        assertEquals(org.slf4j.Logger.class, loggerField.getType());
    }
}