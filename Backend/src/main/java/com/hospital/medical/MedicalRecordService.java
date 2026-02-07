package com.hospital.medical;

import com.hospital.audit.AuditLogService;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.doctor.Doctor;
import com.hospital.doctor.DoctorRepository;
import com.hospital.medical.dto.MedicalRecordCreateRequest;
import com.hospital.medical.dto.MedicalRecordResponse;
import com.hospital.patient.Patient;
import com.hospital.patient.PatientRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicalRecordService {

  private final MedicalRecordRepository medicalRecordRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final AuditLogService auditLogService;

  public MedicalRecordService(
      MedicalRecordRepository medicalRecordRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      AuditLogService auditLogService) {
    this.medicalRecordRepository = medicalRecordRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.auditLogService = auditLogService;
  }

  public MedicalRecordResponse create(MedicalRecordCreateRequest request) {
    Patient patient = patientRepository.findById(request.getPatientId())
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

    Doctor doctor = doctorRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

    MedicalRecord record = MedicalRecord.builder()
        .patient(patient)
        .doctor(doctor)
        .diagnosis(request.getDiagnosis())
        .notes(request.getNotes())
        .visitDate(request.getVisitDate())
        .build();

    MedicalRecord saved = medicalRecordRepository.save(record);
    auditLogService.log("CREATE_MEDICAL_RECORD", "MEDICAL_RECORD", saved.getId().toString(),
        "patientId=" + patient.getId() + ",doctorId=" + doctor.getId());
    return toResponse(saved);
  }

  public List<MedicalRecordResponse> getForPatient(Long patientId) {
    return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId)
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public List<MedicalRecordResponse> getMyRecords() {
    return medicalRecordRepository.findByPatientUserEmailOrderByVisitDateDesc(currentUserEmail())
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public Page<MedicalRecordResponse> getAll(Pageable pageable) {
    return medicalRecordRepository.findAll(pageable).map(this::toResponse);
  }

  private String currentUserEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth == null ? null : auth.getName();
  }

  private MedicalRecordResponse toResponse(MedicalRecord record) {
    return new MedicalRecordResponse(
        record.getId(),
        record.getPatient().getId(),
        record.getDoctor().getId(),
        record.getDiagnosis(),
        record.getNotes(),
        record.getVisitDate()
    );
  }
}
