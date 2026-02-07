package com.hospital.patient.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatientResponse {
  private Long id;
  private Long userId;
  private String name;
  private String email;
  private String phone;
  private String address;
  private LocalDate dateOfBirth;
}
