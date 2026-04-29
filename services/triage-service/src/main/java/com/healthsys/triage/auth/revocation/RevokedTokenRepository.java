package com.healthsys.triage.auth.revocation;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

    boolean existsByTokenId(UUID tokenId);

    boolean existsByTokenIdAndExpiresAtAfter(UUID tokenId, OffsetDateTime now);
}
