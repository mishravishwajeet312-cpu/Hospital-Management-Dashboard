package com.hospital.medical.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MedicalRecordResponse {
  private Long id;
  private Long patientId;
  private Long doctorId;
  private String diagnosis;
  private String notes;
  private LocalDate visitDate;
}
