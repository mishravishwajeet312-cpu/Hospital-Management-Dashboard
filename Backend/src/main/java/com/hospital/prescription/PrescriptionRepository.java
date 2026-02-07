package com.hospital.prescription;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
  List<Prescription> findByMedicalRecordId(Long medicalRecordId);

  void deleteByMedicalRecordIdIn(List<Long> medicalRecordIds);
}
