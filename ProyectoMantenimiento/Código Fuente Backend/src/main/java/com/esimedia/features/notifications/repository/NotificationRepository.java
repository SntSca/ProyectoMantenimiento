package com.esimedia.features.notifications.repository;

import com.esimedia.features.notifications.entity.Notification;
import com.esimedia.features.notifications.enums.NotificationSubtype;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.esimedia.features.notifications.enums.NotificationType;


import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(String userId);

    long countByUserIdAndReadFalseAndDeletedFalse(String userId);

    Optional<Notification> findByIdAndUserIdAndDeletedFalse(String id, String userId);

    boolean existsByUserIdAndSubtypeAndPayloadAndDeletedFalse(
            String userId,
            NotificationSubtype subtype,
            String payload
    );
    boolean existsByUserIdAndSubtypeAndPayloadContaining(String userId, NotificationSubtype subtype, String payload);
    boolean existsByTypeAndSubtypeAndPayloadContaining(
            NotificationType type,
            NotificationSubtype subtype,
            String payload);

        boolean existsByUserIdAndTypeAndSubtypeAndPayloadContaining(
            String userId,
            NotificationType type,
            NotificationSubtype subtype,
            String payloadFragment
    );
    boolean existsByUserIdAndSubtypeAndPayloadContainingAndDeletedFalse(
        String userId,
        NotificationSubtype subtype,
        String payload
);
}
