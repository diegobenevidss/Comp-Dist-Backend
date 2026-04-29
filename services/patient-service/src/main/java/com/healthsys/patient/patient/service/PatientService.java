package com.healthsys.patient.patient.service;

import com.healthsys.patient.messaging.PatientEventPublisher;
import com.healthsys.patient.patient.domain.Patient;
import com.healthsys.patient.patient.dto.CreatePatientRequest;
import com.healthsys.patient.patient.dto.PatientResponse;
import com.healthsys.patient.patient.dto.UpdatePatientRequest;
import com.healthsys.patient.patient.repository.PatientRepository;
import com.healthsys.patient.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientEventPublisher patientEventPublisher;

    public PatientService(PatientRepository patientRepository, PatientEventPublisher patientEventPublisher) {
        this.patientRepository = patientRepository;
        this.patientEventPublisher = patientEventPublisher;
    }

    @Transactional
    public PatientResponse createPatient(CreatePatientRequest request) {
        Patient patient = Patient.builder()
            .fullName(request.name().trim())
            .birthDate(request.birthDate())
            .sex(request.sex())
            .phone(request.phone().trim())
            .active(true)
            .build();

        Patient savedPatient = patientRepository.save(patient);
        patientEventPublisher.publishPatientCreated(savedPatient);
        return toResponse(savedPatient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> listPatients() {
        return patientRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Cacheable(value = "patients", key = "#id")
    @Transactional(readOnly = true)
    public PatientResponse getPatient(UUID id) {
        return patientRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found."));
    }

    @CacheEvict(value = "patients", key = "#id")
    @Transactional
    public PatientResponse updatePatient(UUID id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found."));

        patient.setFullName(request.name().trim());
        patient.setBirthDate(request.birthDate());
        patient.setSex(request.sex());
        patient.setPhone(request.phone().trim());
        patient.setActive(request.active());

        Patient updatedPatient = patientRepository.save(patient);
        patientEventPublisher.publishPatientUpdated(updatedPatient);
        return toResponse(updatedPatient);
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
            patient.getId(),
            patient.getFullName(),
            patient.getBirthDate(),
            patient.getSex(),
            patient.getPhone(),
            patient.isActive(),
            patient.getCreatedAt()
        );
    }
}
