package com.hospital.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreatePatientRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  private String phone;
  private String address;

  @NotNull(message = "Date of birth is required")
  private LocalDate dateOfBirth;
}
