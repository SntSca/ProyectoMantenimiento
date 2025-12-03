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
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UsuarioNormalRepository usuarioNormalRepository;
    private ValidationService validationService;
    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        notificationRepository = mock(NotificationRepository.class);
        usuarioNormalRepository = mock(UsuarioNormalRepository.class);
        validationService = mock(ValidationService.class);
        notificationService = new NotificationService(notificationRepository, usuarioNormalRepository, validationService);
    }

    @Test
    void testGetInbox() {
        Notification n1 = Notification.builder().id("1").userId("u1").build();
        Notification n2 = Notification.builder().id("2").userId("u1").build();

        when(notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc("u1"))
                .thenReturn(List.of(n1, n2));

        List<NotificationDTO> inbox = notificationService.getInbox("u1");

        assertThat(inbox).hasSize(2);
        assertThat(inbox.get(0).getId()).isEqualTo("1");
        verify(notificationRepository).findByUserIdAndDeletedFalseOrderByCreatedAtDesc("u1");
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndReadFalseAndDeletedFalse("u1")).thenReturn(5L);

        long count = notificationService.getUnreadCount("u1");

        assertThat(count).isEqualTo(5L);
        verify(notificationRepository).countByUserIdAndReadFalseAndDeletedFalse("u1");
    }

    @Test
    void testMarkAsReadFoundNotification() {
        Notification notification = spy(Notification.builder().id("n1").userId("u1").build());

        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("n1", "u1"))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead("n1", "u1");

        verify(notification).markAsRead();
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkAsReadNotFound() {
        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("n1", "u1"))
                .thenReturn(Optional.empty());

        notificationService.markAsRead("n1", "u1");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testDeleteNotificationFoundNotification() {
        Notification notification = spy(Notification.builder().id("n1").userId("u1").build());

        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("n1", "u1"))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification("n1", "u1");

        verify(notification).markAsDeleted();
        verify(notificationRepository).save(notification);
    }

    @Test
    void testCreateNotificationsForNewContent() {
        UsuarioNormal user1 = UsuarioNormal.builder()
                .idUsuario("u1")
                .rol(Rol.NORMAL)
                .gustosTags(List.of("accion", "aventura"))
.fechaNacimiento(java.sql.Date.valueOf(LocalDate.of(2000, 1, 1)))
                .build();

        Contenido contenido = Contenido.builder()
                .id("c1")
                .titulo("Nuevo Contenido")
                .visibilidad(true)
                .esVIP(false)
                .restriccionEdad(RestriccionEdad.ADULTOS)
                .tags(List.of("accion"))
                .build();

        when(usuarioNormalRepository.findAll()).thenReturn(List.of(user1));
        when(validationService.calculateAge(user1.getFechaNacimiento())).thenReturn(23);

        notificationService.createNotificationsForNewContent(contenido);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("u1");
        assertThat(saved.getSubtype()).isEqualTo(NotificationSubtype.NEW_CONTENT_RECOMMENDED);
        assertThat(saved.getPayload()).contains("c1");
    }

    @Test
    void testIsExpiringWithinAWeek() {
        Contenido contenido = Contenido.builder()
                .fechaDisponibleHasta(java.util.Date.from(LocalDateTime.now().plusDays(3).atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build();

        boolean expiring = notificationService.isExpiringWithinAWeek(contenido);
        assertThat(expiring).isTrue();
    }

    @Test
    void testIsNotExpiringWithinAWeek() {
        Contenido contenido = Contenido.builder()
                .fechaDisponibleHasta(java.util.Date.from(LocalDateTime.now().plusDays(10).atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build();

        boolean expiring = notificationService.isExpiringWithinAWeek(contenido);
        assertThat(expiring).isFalse();
    }
}
