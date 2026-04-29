package com.healthsys.triage.auth.revocation;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenRevocationService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void revokeToken(UUID tokenId, UUID userId, Instant expiresAt) {
        if (revokedTokenRepository.existsByTokenId(tokenId)) {
            return;
        }
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setTokenId(tokenId);
        revokedToken.setUserId(userId);
        revokedToken.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
        revokedToken.setExpiresAt(OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
        revokedTokenRepository.save(revokedToken);
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(UUID tokenId) {
        return revokedTokenRepository.existsByTokenIdAndExpiresAtAfter(tokenId, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
