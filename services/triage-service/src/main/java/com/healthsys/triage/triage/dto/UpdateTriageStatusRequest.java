package com.healthsys.triage.triage.dto;

import com.healthsys.triage.triage.domain.TriageStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTriageStatusRequest(
    @NotNull TriageStatus status,
    String notes,
    String attendedBy
) {
}
