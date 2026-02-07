package com.hospital.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateDoctorRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Specialization is required")
  private String specialization;

  private String phone;
}
