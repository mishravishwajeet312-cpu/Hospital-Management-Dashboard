package com.hospital.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionCreateRequest {
  @NotNull(message = "Medical record id is required")
  private Long medicalRecordId;

  @NotBlank(message = "Medicine name is required")
  private String medicineName;

  @NotBlank(message = "Dosage is required")
  private String dosage;

  @NotBlank(message = "Duration is required")
  private String duration;

  private String instructions;
}
