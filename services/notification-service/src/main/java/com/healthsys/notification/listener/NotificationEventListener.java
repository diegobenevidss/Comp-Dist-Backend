package com.healthsys.notification.listener;

import com.healthsys.notification.service.NotificationService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.messaging.queue}")
    public void handleNotificationEvent(
        Map<String, Object> payload,
        @Header(value = "amqp_receivedRoutingKey", required = false) String routingKey
    ) {
        String key = routingKey != null ? routingKey : "unknown";
        LOGGER.info("Notification event received: routingKey={} type={}", key, payload.get("eventType"));
        notificationService.persistEvent(payload, key);
    }
}
