package com.healthsys.notification.dto;

import com.healthsys.notification.domain.NotificationStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String eventType,
    String resourceId,
    String routingKey,
    String payload,
    NotificationStatus status,
    OffsetDateTime occurredAt,
    OffsetDateTime readAt
) {
}
