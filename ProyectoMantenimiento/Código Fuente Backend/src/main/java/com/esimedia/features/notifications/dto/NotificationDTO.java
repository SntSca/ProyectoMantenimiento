package com.esimedia.features.notifications.dto;

import java.util.Map;

public record NotificationDTO(
        String id,
        String type,
        String subtype,
        String title,
        String body,
        Map<String, Object> payload,
        boolean read,
        String createdAt
) {}