package com.healthsys.triage.auth.revocation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenRevocationListener {

    private final TokenRevocationService tokenRevocationService;

    public AuthTokenRevocationListener(TokenRevocationService tokenRevocationService) {
        this.tokenRevocationService = tokenRevocationService;
    }

    @RabbitListener(queues = "${app.messaging.auth-logout-queue}")
    @SuppressWarnings("unchecked")
    public void handleRevokedToken(Map<String, Object> event) {
        Object payloadValue = event.get("payload");
        if (!(payloadValue instanceof Map<?, ?> payload)) {
            return;
        }
        Object tokenId = payload.get("tokenId");
        Object userId = payload.get("userId");
        Object expiresAt = payload.get("expiresAt");

        if (tokenId == null || userId == null || expiresAt == null) {
            return;
        }
        tokenRevocationService.revokeToken(
            UUID.fromString(tokenId.toString()),
            UUID.fromString(userId.toString()),
            Instant.parse(expiresAt.toString())
        );
    }
}
