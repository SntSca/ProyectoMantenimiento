package com.esimedia.features.notifications.services;

import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.entity.Contenido;
import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.notifications.dto.NotificationDTO;
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
    
    @InjectMocks private NotificationService notificationService;

    private UsuarioNormal usuarioNormal;
    private Notification notification;
    private Contenido contenido;

    @BeforeEach
    void setUp() {
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

        contenido = new Contenido(); // Ajusta constructor según tu entidad
        contenido.setId("content-1");
        contenido.setTitulo("Canción Rock");
        contenido.setTags(List.of("musica", "rock"));
        contenido.setVisibilidad(true);
        contenido.setEsVIP(false);
        contenido.setRestriccionEdad(RestriccionEdad.MAYOR_18);
    }

    @Test
    void getInbox_shouldReturnNotificationsOrderedByCreatedAtDesc() {
        when(notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user-123"))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result = notificationService.getInbox("user-123");

        assertEquals(1, result.size());
        assertEquals("notif-1", result.get(0).id());
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

    @Test
    void markAsRead_shouldMarkNotificationAsRead_whenExists() {
        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("notif-1", "user-123"))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead("notif-1", "user-123");

        verify(notification).markAsRead();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldDoNothing_whenNotificationNotFound() {
        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("notif-1", "user-123"))
                .thenReturn(Optional.empty());

        notificationService.markAsRead("notif-1", "user-123");

        verifyNoInteractions(notification);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotificationsForNewContent_shouldCreateRecommendedForMatchingTastes() {
        when(usuarioNormalRepository.findAll()).thenReturn(List.of(usuarioNormal));
        when(validationService.calculateAge(any())).thenReturn(25);

        contenido.setTags(List.of("rock")); // Match con gustos del usuario
        notificationService.createNotificationsForNewContent(contenido);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(NotificationSubtype.NEW_CONTENT_RECOMMENDED, saved.getSubtype());
        assertEquals("Lo más recomendado para ti", saved.getTitle());
    }

    @Test
    void createNotificationsForNewContent_shouldCreateGenericForNonMatchingTastes() {
        when(usuarioNormalRepository.findAll()).thenReturn(List.of(usuarioNormal));
        when(validationService.calculateAge(any())).thenReturn(25);

        contenido.setTags(List.of("pop")); // NO match con gustos "musica", "rock"
        notificationService.createNotificationsForNewContent(contenido);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(NotificationSubtype.NEW_CONTENT, saved.getSubtype());
        assertEquals("Esto te podría interesar", saved.getTitle());
    }

   
    @Test
    void isExpiringWithinAWeek_shouldReturnTrue_whenWithin7Days() {
        Date fechaExpiracion = Date.from(LocalDateTime.now().plusDays(3)
                .atZone(java.time.ZoneId.systemDefault()).toInstant());
        contenido.setFechaDisponibleHasta(fechaExpiracion);

        boolean result = notificationService.isExpiringWithinAWeek(contenido);

        assertTrue(result);
    }

    @Test
void isExpiringWithinAWeek_shouldReturnFalse_whenExpired() {
        Date fechaExpiracion = Date.from(LocalDateTime.now().minusDays(1)
                .atZone(java.time.ZoneId.systemDefault()).toInstant());
        contenido.setFechaDisponibleHasta(fechaExpiracion);

        boolean result = notificationService.isExpiringWithinAWeek(contenido);

        assertFalse(result);
    }
}
