package com.healthsys.patient.patient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthsys.patient.messaging.PatientEventPublisher;
import com.healthsys.patient.patient.domain.Patient;
import com.healthsys.patient.patient.domain.Sex;
import com.healthsys.patient.patient.dto.CreatePatientRequest;
import com.healthsys.patient.patient.dto.PatientResponse;
import com.healthsys.patient.patient.dto.UpdatePatientRequest;
import com.healthsys.patient.patient.repository.PatientRepository;
import com.healthsys.patient.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientServiceUnitTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientEventPublisher patientEventPublisher;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, patientEventPublisher);
    }

    @Test
    void shouldCreatePatientAndPublishEvent() {
        CreatePatientRequest request = new CreatePatientRequest(
            "Maria Oliveira",
            LocalDate.of(1990, 5, 17),
            Sex.FEMALE,
            "85999990000"
        );
        Patient savedPatient = Patient.builder()
            .id(UUID.randomUUID())
            .fullName("Maria Oliveira")
            .birthDate(LocalDate.of(1990, 5, 17))
            .sex(Sex.FEMALE)
            .phone("85999990000")
            .active(true)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponse response = patientService.createPatient(request);

        assertThat(response.name()).isEqualTo("Maria Oliveira");
        assertThat(response.active()).isTrue();
        verify(patientEventPublisher).publishPatientCreated(savedPatient);
    }

    @Test
    void shouldUpdatePatientWithoutDeletingRecord() {
        UUID patientId = UUID.randomUUID();
        Patient patient = Patient.builder()
            .id(patientId)
            .fullName("Maria Oliveira")
            .birthDate(LocalDate.of(1990, 5, 17))
            .sex(Sex.FEMALE)
            .phone("85999990000")
            .active(true)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
        UpdatePatientRequest request = new UpdatePatientRequest(
            "Maria Oliveira Atualizada",
            LocalDate.of(1990, 5, 17),
            Sex.FEMALE,
            "85888887777",
            false
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        PatientResponse response = patientService.updatePatient(patientId, request);

        assertThat(response.name()).isEqualTo("Maria Oliveira Atualizada");
        assertThat(response.active()).isFalse();
        verify(patientEventPublisher).publishPatientUpdated(patient);
    }

    @Test
    void shouldThrowWhenPatientDoesNotExist() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatient(patientId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Patient not found.");
    }
}
