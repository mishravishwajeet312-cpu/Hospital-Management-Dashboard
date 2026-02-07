package com.hospital.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordCreateRequest {
  @NotNull(message = "Patient id is required")
  private Long patientId;

  @NotBlank(message = "Diagnosis is required")
  private String diagnosis;

  private String notes;

  @NotNull(message = "Visit date is required")
  private LocalDate visitDate;
}
