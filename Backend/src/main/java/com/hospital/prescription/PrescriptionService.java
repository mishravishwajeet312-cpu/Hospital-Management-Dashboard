package com.hospital.prescription;

import com.hospital.audit.AuditLogService;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.medical.MedicalRecord;
import com.hospital.medical.MedicalRecordRepository;
import com.hospital.prescription.dto.PrescriptionCreateRequest;
import com.hospital.prescription.dto.PrescriptionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionService {

  private final PrescriptionRepository prescriptionRepository;
  private final MedicalRecordRepository medicalRecordRepository;
  private final AuditLogService auditLogService;

  public PrescriptionService(
      PrescriptionRepository prescriptionRepository,
      MedicalRecordRepository medicalRecordRepository,
      AuditLogService auditLogService) {
    this.prescriptionRepository = prescriptionRepository;
    this.medicalRecordRepository = medicalRecordRepository;
    this.auditLogService = auditLogService;
  }

  public PrescriptionResponse create(PrescriptionCreateRequest request) {
    MedicalRecord record = medicalRecordRepository.findById(request.getMedicalRecordId())
        .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

    Prescription prescription = Prescription.builder()
        .medicalRecord(record)
        .medicineName(request.getMedicineName())
        .dosage(request.getDosage())
        .duration(request.getDuration())
        .instructions(request.getInstructions())
        .build();

    Prescription saved = prescriptionRepository.save(prescription);
    auditLogService.log("CREATE_PRESCRIPTION", "PRESCRIPTION", saved.getId().toString(),
        "medicalRecordId=" + record.getId());
    return toResponse(saved);
  }

  private PrescriptionResponse toResponse(Prescription prescription) {
    return new PrescriptionResponse(
        prescription.getId(),
        prescription.getMedicalRecord().getId(),
        prescription.getMedicineName(),
        prescription.getDosage(),
        prescription.getDuration(),
        prescription.getInstructions()
    );
  }
}
