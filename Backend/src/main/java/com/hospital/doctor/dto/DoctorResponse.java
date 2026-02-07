package com.hospital.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorResponse {
  private Long id;
  private Long userId;
  private String name;
  private String email;
  private String specialization;
  private String phone;
}
