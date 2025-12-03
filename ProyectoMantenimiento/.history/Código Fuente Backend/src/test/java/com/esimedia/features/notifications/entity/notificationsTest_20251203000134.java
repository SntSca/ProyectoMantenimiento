package com.esimedia.features.notifications.entity;

import com.esimedia.features.notifications.enums.NotificationSubtype;
import com.esimedia.features.notifications.enums.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void testDefaultValues() {
        Notification notification = new Notification();

        assertThat(notification.isRead()).isFalse();
        assertThat(notification.isDeleted()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getReadAt()).isNull();
        assertThat(notification.getDeletedAt()).isNull();
    }

    @Test
    void testMarkAsRead() {
        Notification notification = new Notification();
        assertThat(notification.isRead()).isFalse();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }

    @Test
    void testMarkAsReadAlreadyRead() {
        Notification notification = new Notification();
        notification.markAsRead();
        LocalDateTime firstReadAt = notification.getReadAt();

        // Intentar marcar como leído nuevamente
        notification.markAsRead();

        // Debe mantenerse igual
        assertThat(notification.getReadAt()).isEqualTo(firstReadAt);
    }

    @Test
    void testMarkAsDeleted() {
        Notification notification = new Notification();
        assertThat(notification.isDeleted()).isFalse();

        notification.markAsDeleted();

        assertThat(notification.isDeleted()).isTrue();
        assertThat(notification.getDeletedAt()).isNotNull();
    }

    @Test
    void testMarkAsDeletedAlreadyDeleted() {
        Notification notification = new Notification();
        notification.markAsDeleted();
        LocalDateTime firstDeletedAt = notification.getDeletedAt();

        // Intentar marcar como eliminado nuevamente
        notification.markAsDeleted();

        // Debe mantenerse igual
        assertThat(notification.getDeletedAt()).isEqualTo(firstDeletedAt);
    }

    @Test
    void testBuilderAndAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Notification notification = Notification.builder()
                .id("1")
                .userId("user123")
                .type(NotificationType.NOTIFICATION)
                .subtype(NotificationSubtype.)
                .title("Título")
                .body("Cuerpo del mensaje")
                .payload("{\"key\":\"value\"}")
                .read(true)
                .deleted(true)
                .createdAt(now)
                .readAt(now)
                .deletedAt(now)
                .build();

        assertThat(notification.getId()).isEqualTo("1");
        assertThat(notification.getUserId()).isEqualTo("user123");
        assertThat(notification.getType()).isEqualTo(NotificationType.INFO);
        assertThat(notification.getSubtype()).isEqualTo(NotificationSubtype.GENERAL);
        assertThat(notification.getTitle()).isEqualTo("Título");
        assertThat(notification.getBody()).isEqualTo("Cuerpo del mensaje");
        assertThat(notification.getPayload()).isEqualTo("{\"key\":\"value\"}");
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.isDeleted()).isTrue();
        assertThat(notification.getCreatedAt()).isEqualTo(now);
        assertThat(notification.getReadAt()).isEqualTo(now);
        assertThat(notification.getDeletedAt()).isEqualTo(now);
    }
}
