package com.healthsys.triage.triage.service;

import com.healthsys.triage.messaging.TriageEventPublisher;
import com.healthsys.triage.shared.exception.ResourceNotFoundException;
import com.healthsys.triage.triage.domain.Triage;
import com.healthsys.triage.triage.domain.TriageStatus;
import com.healthsys.triage.triage.dto.CreateTriageRequest;
import com.healthsys.triage.triage.dto.TriageResponse;
import com.healthsys.triage.triage.dto.UpdateTriageStatusRequest;
import com.healthsys.triage.triage.repository.TriageRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TriageService {

    private final TriageRepository triageRepository;
    private final TriageEventPublisher eventPublisher;

    public TriageService(TriageRepository triageRepository, TriageEventPublisher eventPublisher) {
        this.triageRepository = triageRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TriageResponse createTriage(CreateTriageRequest request) {
        Triage triage = Triage.builder()
            .patientId(request.patientId())
            .patientName(request.patientName().trim())
            .priority(request.priority())
            .chiefComplaint(request.chiefComplaint().trim())
            .notes(request.notes())
            .build();

        Triage saved = triageRepository.save(triage);
        eventPublisher.publishTriageCreated(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TriageResponse> listTriages() {
        return triageRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TriageResponse getTriage(UUID id) {
        return triageRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Triage not found."));
    }

    @Transactional(readOnly = true)
    public List<TriageResponse> getTriagesByPatient(UUID patientId) {
        return triageRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public TriageResponse updateStatus(UUID id, UpdateTriageStatusRequest request) {
        Triage triage = triageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Triage not found."));

        triage.setStatus(request.status());
        if (request.notes() != null) triage.setNotes(request.notes());
        if (request.attendedBy() != null) triage.setAttendedBy(request.attendedBy());

        if (request.status() == TriageStatus.IN_PROGRESS && triage.getAttendedAt() == null) {
            triage.setAttendedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }

        Triage updated = triageRepository.save(triage);
        eventPublisher.publishTriageUpdated(updated);
        return toResponse(updated);
    }

    private TriageResponse toResponse(Triage triage) {
        return new TriageResponse(
            triage.getId(),
            triage.getPatientId(),
            triage.getPatientName(),
            triage.getPriority(),
            triage.getStatus(),
            triage.getChiefComplaint(),
            triage.getNotes(),
            triage.getAttendedBy(),
            triage.getCreatedAt(),
            triage.getUpdatedAt(),
            triage.getAttendedAt()
        );
    }
}
