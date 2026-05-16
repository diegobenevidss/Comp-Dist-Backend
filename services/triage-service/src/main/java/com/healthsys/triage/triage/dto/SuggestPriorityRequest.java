package com.healthsys.triage.triage.dto;

import jakarta.validation.constraints.NotBlank;

public record SuggestPriorityRequest(
    @NotBlank String chiefComplaint,
    String notes
) {
}