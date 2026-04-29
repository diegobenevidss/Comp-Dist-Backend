package com.healthsys.triage.triage.controller;

import com.healthsys.triage.triage.dto.CreateTriageRequest;
import com.healthsys.triage.triage.dto.TriageResponse;
import com.healthsys.triage.triage.dto.UpdateTriageStatusRequest;
import com.healthsys.triage.triage.service.TriageService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triages")
public class TriageController {

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public TriageResponse createTriage(@Valid @RequestBody CreateTriageRequest request) {
        return triageService.createTriage(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public List<TriageResponse> listTriages() {
        return triageService.listTriages();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public TriageResponse getTriage(@PathVariable UUID id) {
        return triageService.getTriage(id);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public List<TriageResponse> getTriagesByPatient(@PathVariable UUID patientId) {
        return triageService.getTriagesByPatient(patientId);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEALTH_PROFESSIONAL')")
    public TriageResponse updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateTriageStatusRequest request
    ) {
        return triageService.updateStatus(id, request);
    }
}
