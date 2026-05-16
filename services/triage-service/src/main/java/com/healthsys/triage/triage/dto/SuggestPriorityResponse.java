package com.healthsys.triage.triage.dto;

import com.healthsys.triage.triage.domain.Priority;

public record SuggestPriorityResponse(
    Priority suggestedPriority,
    String reasoning
) {
}