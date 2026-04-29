package com.healthsys.triage.triage.dto;

import com.healthsys.triage.triage.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateTriageRequest(
    @NotNull UUID patientId,
    @NotBlank String patientName,
    @NotNull Priority priority,
    @NotBlank String chiefComplaint,
    String notes
) {
}
