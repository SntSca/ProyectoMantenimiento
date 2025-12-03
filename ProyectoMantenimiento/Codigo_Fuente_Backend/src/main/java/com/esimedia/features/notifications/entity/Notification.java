package com.esimedia.features.notifications.entity;

import com.esimedia.features.notifications.enums.NotificationSubtype;
import com.esimedia.features.notifications.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Field("userId")
    private String userId; // idUsuario de UsuarioNormal

    @Field("type")
    private NotificationType type;

    @Field("subtype")
    private NotificationSubtype subtype;

    @Field("title")
    private String title;

    @Field("body")
    private String body;

    @Field("payload")
    private String payload;

    @Field("read")
    @Builder.Default
    private boolean read = false;

    @Field("deleted")
    @Builder.Default
    private boolean deleted = false;

    @Field("createdAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Field("readAt")
    private LocalDateTime readAt;

    @Field("deletedAt")
    private LocalDateTime deletedAt;

    public void markAsRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public void markAsDeleted() {
        if (!this.deleted) {
            this.deleted = true;
            this.deletedAt = LocalDateTime.now();
        }
    }
}
