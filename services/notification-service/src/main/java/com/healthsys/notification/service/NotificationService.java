package com.healthsys.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthsys.notification.domain.Notification;
import com.healthsys.notification.domain.NotificationStatus;
import com.healthsys.notification.dto.NotificationResponse;
import com.healthsys.notification.repository.NotificationRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void persistEvent(Map<String, Object> event, String routingKey) {
        try {
            String eventType = event.getOrDefault("eventType", "UNKNOWN").toString();
            Object resourceId = event.get("resourceId");
            String payload = objectMapper.writeValueAsString(event);

            Notification notification = Notification.builder()
                .eventType(eventType)
                .resourceId(resourceId != null ? resourceId.toString() : null)
                .routingKey(routingKey)
                .payload(payload)
                .status(NotificationStatus.UNREAD)
                .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

            notificationRepository.save(notification);
            LOGGER.info("Persisted notification: type={} routingKey={}", eventType, routingKey);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize event payload for routingKey={}", routingKey, e);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listAll() {
        return notificationRepository.findAllByOrderByOccurredAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listUnread() {
        return notificationRepository.findByStatusOrderByOccurredAtDesc(NotificationStatus.UNREAD).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID id) {
        return notificationRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new NoSuchElementException("Notification not found."));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Notification not found."));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(OffsetDateTime.now(ZoneOffset.UTC));
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
            n.getId(), n.getEventType(), n.getResourceId(),
            n.getRoutingKey(), n.getPayload(), n.getStatus(),
            n.getOccurredAt(), n.getReadAt()
        );
    }
}
