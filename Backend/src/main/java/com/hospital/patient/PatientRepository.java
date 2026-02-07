package com.hospital.patient;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
  Optional<Patient> findByUserEmail(String email);
  Optional<Patient> findByUserId(Long userId);
}
