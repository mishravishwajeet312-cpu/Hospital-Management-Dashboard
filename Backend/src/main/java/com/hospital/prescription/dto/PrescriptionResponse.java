package com.hospital.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrescriptionResponse {
  private Long id;
  private Long medicalRecordId;
  private String medicineName;
  private String dosage;
  private String duration;
  private String instructions;
}
