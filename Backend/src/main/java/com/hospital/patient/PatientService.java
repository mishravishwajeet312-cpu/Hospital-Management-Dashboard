package com.hospital.patient;

import com.hospital.patient.dto.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientService {

  private final PatientRepository patientRepository;

  public PatientService(PatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  public Page<PatientResponse> search(String name, String phone, Pageable pageable) {
    Specification<Patient> spec = Specification.where(PatientSpecifications.nameLike(name))
        .and(PatientSpecifications.phoneLike(phone));

    return patientRepository.findAll(spec, pageable)
        .map(this::toResponse);
  }

  private PatientResponse toResponse(Patient patient) {
    return new PatientResponse(
        patient.getId(),
        patient.getUser().getId(),
        patient.getUser().getName(),
        patient.getUser().getEmail(),
        patient.getPhone(),
        patient.getAddress(),
        patient.getDateOfBirth()
    );
  }
}
