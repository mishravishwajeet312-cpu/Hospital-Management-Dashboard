package com.hospital.medical;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
  List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId);
  List<MedicalRecord> findByPatientUserEmailOrderByVisitDateDesc(String email);

  @Query("select m.id from MedicalRecord m where m.doctor.id = :doctorId")
  List<Long> findIdsByDoctorId(@Param("doctorId") Long doctorId);

  @Query("select m.id from MedicalRecord m where m.patient.id = :patientId")
  List<Long> findIdsByPatientId(@Param("patientId") Long patientId);
}
