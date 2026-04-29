package com.healthsys.triage.triage.repository;

import com.healthsys.triage.triage.domain.Triage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TriageRepository extends JpaRepository<Triage, UUID> {

    List<Triage> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<Triage> findAllByOrderByCreatedAtDesc();
}
