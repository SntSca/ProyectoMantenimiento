package com.esimedia.features.notifications.services;

import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.entity.Contenido;
import com.esimedia.features.notifications.entity.Notification;
import com.esimedia.features.notifications.enums.NotificationSubtype;
import com.esimedia.features.notifications.enums.NotificationType;
import com.esimedia.features.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UsuarioNormalRepository usuarioNormalRepository;
    @Mock private ValidationService validationService;
    @Mock private Contenido contenido;
    
    @InjectMocks private NotificationService notificationService;

    private UsuarioNormal usuarioNormal;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // ← SIN stubs aquí para evitar UnnecessaryStubbingException
        usuarioNormal = new UsuarioNormal();
        usuarioNormal.setIdUsuario("user-123");
        usuarioNormal.setRol(Rol.NORMAL);
        usuarioNormal.setGustosTags(List.of("musica", "rock"));

        notification = Notification.builder()
                .id("notif-1")
                .userId("user-123")
                .type(NotificationType.NOTIFICATION)
                .subtype(NotificationSubtype.NEW_CONTENT)
                .title("Test")
                .body("Test body")
                .payload("{\"contentId\":\"content-1\"}")
                .build();
    }

    @Test
    void markAsRead_shouldMarkNotificationAsRead_whenExists() {
        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("notif-1", "user-123"))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead("notif-1", "user-123");

        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldDoNothing_whenNotificationNotFound() {
        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("notif-1", "user-123"))
                .thenReturn(Optional.empty());

        notificationService.markAsRead("notif-1", "user-123");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotificationsForNewContent_shouldCreateRecommendedForMatchingTastes() {
        // ← TODOS los stubs ESPECÍFICOS para este test
        when(usuarioNormalRepository.findAll()).thenReturn(List.of(usuarioNormal));
        when(validationService.calculateAge(any())).thenReturn(25);
        when(contenido.getId()).thenReturn("content-1");
        when(contenido.getTitulo()).thenReturn("Canción Rock");
        when(contenido.isVisibilidad()).thenReturn(true);
        when(contenido.isEsVIP()).thenReturn(false);
        when(contenido.getRestriccionEdad()).thenReturn(null);
        when(contenido.getTags()).thenReturn(List.of("rock")); // Match con gustos

        notificationService.createNotificationsForNewContent(contenido);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(NotificationSubtype.NEW_CONTENT_RECOMMENDED, saved.getSubtype());
        assertEquals("Lo más recomendado para ti", saved.getTitle());
    }

    @Test
    void createNotificationsForNewContent_shouldCreateGenericForNonMatchingTastes() {
        // ← TODOS los stubs ESPECÍFICOS para este test
        when(usuarioNormalRepository.findAll()).thenReturn(List.of(usuarioNormal));
        when(validationService.calculateAge(any())).thenReturn(25);
        when(contenido.getId()).thenReturn("content-1");
        when(contenido.getTitulo()).thenReturn("Canción Pop");
        when(contenido.isVisibilidad()).thenReturn(true);
        when(contenido.isEsVIP()).thenReturn(false);
        when(contenido.getRestriccionEdad()).thenReturn(null);
        when(contenido.getTags()).thenReturn(List.of("pop")); // NO match

        notificationService.createNotificationsForNewContent(contenido);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(NotificationSubtype.NEW_CONTENT, saved.getSubtype());
        assertEquals("Esto te podría interesar", saved.getTitle());
    }

    @Test
    void isExpiringWithinAWeek_shouldReturnTrue_whenWithin7Days() {
        // ← SOLO los stubs necesarios para este test
        Date fechaExpiracion = Date.from(LocalDateTime.now().plusDays(3)
                .atZone(ZoneId.systemDefault()).toInstant());
        when(contenido.getFechaDisponibleHasta()).thenReturn(fechaExpiracion);

        boolean result = notificationService.isExpiringWithinAWeek(contenido);

        assertTrue(result);
    }

    @Test
    void getInbox_shouldReturnNotificationsOrderedByCreatedAtDesc() {
        when(notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user-123"))
                .thenReturn(List.of(notification));

        List<?> result = notificationService.getInbox("user-123");

        assertEquals(1, result.size());
        verify(notificationRepository).findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user-123");
    }

    @Test
    void getUnreadCount_shouldReturnUnreadNotificationsCount() {
        when(notificationRepository.countByUserIdAndReadFalseAndDeletedFalse("user-123"))
                .thenReturn(3L);

        long count = notificationService.getUnreadCount("user-123");

        assertEquals(3L, count);
        verify(notificationRepository).countByUserIdAndReadFalseAndDeletedFalse("user-123");
    }
}
