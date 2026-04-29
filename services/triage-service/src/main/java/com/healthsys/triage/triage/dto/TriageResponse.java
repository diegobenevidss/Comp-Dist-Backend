package com.healthsys.triage.triage.dto;

import com.healthsys.triage.triage.domain.Priority;
import com.healthsys.triage.triage.domain.TriageStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TriageResponse(
    UUID id,
    UUID patientId,
    String patientName,
    Priority priority,
    TriageStatus status,
    String chiefComplaint,
    String notes,
    String attendedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime attendedAt
) {
}
