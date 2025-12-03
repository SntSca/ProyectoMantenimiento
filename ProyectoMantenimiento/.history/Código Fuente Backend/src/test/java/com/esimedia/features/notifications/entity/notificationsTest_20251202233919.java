package com.esimedia.features.notifications.entity;

import com.esimedia.features.notifications.enums.NotificationSubtype;
import com.esimedia.features.notifications.enums.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void builderShouldSetDefaults() {
        Notification notification = Notification.builder()
                .id("1")
                .userId("user-1")
                .type(NotificationType.GENERAL) // ajusta al enum real
                .subtype(NotificationSubtype.INFO) // ajusta al enum real
                .title("Título")
                .body("Cuerpo")
                .payload("{}")
                .build();

        assertFalse(notification.isRead());
        assertFalse(notification.isDeleted());
        assertNotNull(notification.getCreatedAt());
        assertNull(notification.getReadAt());
        assertNull(notification.getDeletedAt());
    }

    @Test
    void markAsReadShouldSetReadTrueAndReadAt() {
        Notification notification = Notification.builder()
                .id("1")
                .userId("user-1")
                .type(NotificationType.GENERAL)
                .subtype(NotificationSubtype.INFO)
                .title("Título")
                .body("Cuerpo")
                .payload("{}")
                .build();

        assertFalse(notification.isRead());
        assertNull(notification.getReadAt());

        notification.markAsRead();

        assertTrue(notification.isRead());
        assertNotNull(notification.getReadAt());
    }

    @Test
    void markAsReadShouldNotOverrideReadAtIfAlreadyRead() {
        Notification notification = Notification.builder()
                .id("1")
                .userId("user-1")
                .type(NotificationType.GENERAL)
                .subtype(NotificationSubtype.INFO)
                .title("Título")
                .body("Cuerpo")
                .payload("{}")
                .build();

        notification.markAsRead();
        LocalDateTime firstReadAt = notification.getReadAt();

        notification.markAsRead();
        LocalDateTime secondReadAt = notification.getReadAt();

        assertEquals(firstReadAt, secondReadAt);
    }

    @Test
    void markAsDeletedShouldSetDeletedTrueAndDeletedAt() {
        Notification notification = Notification.builder()
                .id("1")
                .userId("user-1")
                .type(NotificationType.GENERAL)
                .subtype(NotificationSubtype.INFO)
                .title("Título")
                .body("Cuerpo")
                .payload("{}")
                .build();

        assertFalse(notification.isDeleted());
        assertNull(notification.getDeletedAt());

        notification.markAsDeleted();

        assertTrue(notification.isDeleted());
        assertNotNull(notification.getDeletedAt());
    }

    @Test
    void markAsDeletedShouldNotOverrideDeletedAtIfAlreadyDeleted() {
        Notification notification = Notification.builder()
                .id("1")
                .userId("user-1")
                .type(NotificationType.GENERAL)
                .subtype(NotificationSubtype.INFO)
                .title("Título")
                .body("Cuerpo")
                .payload("{}")
                .build();

        notification.markAsDeleted();
        LocalDateTime firstDeletedAt = notification.getDeletedAt();

        notification.markAsDeleted();
        LocalDateTime secondDeletedAt = notification.getDeletedAt();

        assertEquals(firstDeletedAt, secondDeletedAt);
    }
}
