package com.healthsys.triage.messaging;

import com.healthsys.triage.config.MessagingProperties;
import com.healthsys.triage.triage.domain.Triage;
import java.time.Instant;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TriageEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties messagingProperties;

    public TriageEventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties messagingProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.messagingProperties = messagingProperties;
    }

    public void publishTriageCreated(Triage triage) {
        publish("TRIAGE_CREATED", "healthsys.triage.created", triage);
    }

    public void publishTriageUpdated(Triage triage) {
        publish("TRIAGE_UPDATED", "healthsys.triage.updated", triage);
    }

    private void publish(String eventType, String routingKey, Triage triage) {
        Map<String, Object> payload = Map.of(
            "eventType", eventType,
            "resourceId", triage.getId().toString(),
            "occurredAt", Instant.now().toString(),
            "payload", Map.of(
                "patientId", triage.getPatientId().toString(),
                "patientName", triage.getPatientName(),
                "priority", triage.getPriority().name(),
                "status", triage.getStatus().name(),
                "chiefComplaint", triage.getChiefComplaint()
            )
        );
        rabbitTemplate.convertAndSend(messagingProperties.getExchange(), routingKey, payload);
    }
}
