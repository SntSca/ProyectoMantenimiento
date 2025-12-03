package com.esimedia.features.notifications.entity;

import com.esimedia.features.notifications.enums.NotificationSubtype;
import com.esimedia.features.notifications.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    private Notification notification;

    // Configura un objeto Notification antes de cada prueba
    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id("noti-001")
                .userId("user-abc")
                .type(NotificationType.COMMENT)
                .subtype(NotificationSubtype.REPLY)
                .title("New Reply")
                .body("Someone replied to your post.")
                .payload("{ \"postId\": \"123\" }")
                // Las propiedades 'read', 'deleted', y 'createdAt' se inicializan por defecto
                .build();
    }

    // --- Tests de Inicialización y Builder ---

    @Test
    @DisplayName("Debe crear la notificación con los valores por defecto correctos")
    void shouldInitializeWithDefaults() {
        // Verifica que las propiedades booleanas sean false por defecto
        assertFalse(notification.isRead(), "La propiedad 'read' debe ser false por defecto.");
        assertFalse(notification.isDeleted(), "La propiedad 'deleted' debe ser false por defecto.");

        // Verifica que 'createdAt' esté establecido y sea reciente
        assertNotNull(notification.getCreatedAt(), "La propiedad 'createdAt' no debe ser nula.");
        assertTrue(notification.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                "La fecha de creación debe ser muy cercana a la hora actual.");

        // Verifica que las propiedades de fecha 'At' no estén establecidas inicialmente
        assertNull(notification.getReadAt(), "La propiedad 'readAt' debe ser nula.");
        assertNull(notification.getDeletedAt(), "La propiedad 'deletedAt' debe ser nula.");
    }

    // --- Tests de markAsRead() ---

    @Test
    @DisplayName("Debe marcar la notificación como leída y registrar readAt")
    void shouldMarkAsReadAndSetReadAt() {
        // Ejecuta el método a probar
        notification.markAsRead();
        
        // Verifica los cambios
        assertTrue(notification.isRead(), "La notificación debe estar marcada como leída.");
        assertNotNull(notification.getReadAt(), "La propiedad 'readAt' debe tener un valor.");
    }

    @Test
    @DisplayName("No debe cambiar readAt si ya está marcada como leída")
    void shouldNotChangeReadAtIfAlreadyRead() throws InterruptedException {
        // Paso 1: Marcar como leída por primera vez
        notification.markAsRead();
        LocalDateTime firstReadTime = notification.getReadAt();

        // Esperar un momento para asegurar que la hora de la segunda llamada sería diferente
        Thread.sleep(10); 
        
        // Paso 2: Intentar marcar como leída de nuevo
        notification.markAsRead();

        // Verifica que la hora de lectura sea la misma que la primera vez
        assertEquals(firstReadTime, notification.getReadAt(),
                "El timestamp 'readAt' no debe cambiar si ya está leída.");
    }

    // --- Tests de markAsDeleted() ---

    @Test
    @DisplayName("Debe marcar la notificación como eliminada y registrar deletedAt")
    void shouldMarkAsDeletedAndSetDeletedAt() {
        // Ejecuta el método a probar
        notification.markAsDeleted();
        
        // Verifica los cambios
        assertTrue(notification.isDeleted(), "La notificación debe estar marcada como eliminada.");
        assertNotNull(notification.getDeletedAt(), "La propiedad 'deletedAt' debe tener un valor.");
    }
    
    @Test
    @DisplayName("No debe cambiar deletedAt si ya está marcada como eliminada")
    void shouldNotChangeDeletedAtIfAlreadyDeleted() throws InterruptedException {
        // Paso 1: Marcar como eliminada por primera vez
        notification.markAsDeleted();
        LocalDateTime firstDeletedTime = notification.getDeletedAt();

        // Esperar un momento
        Thread.sleep(10); 
        
        // Paso 2: Intentar marcar como eliminada de nuevo
        notification.markAsDeleted();

        // Verifica que la hora de eliminación sea la misma que la primera vez
        assertEquals(firstDeletedTime, notification.getDeletedAt(),
                "El timestamp 'deletedAt' no debe cambiar si ya está eliminada.");
    }
}