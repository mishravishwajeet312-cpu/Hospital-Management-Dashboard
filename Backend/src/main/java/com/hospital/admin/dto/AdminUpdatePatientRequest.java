package com.hospital.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdatePatientRequest {

  private String name;

  @Email(message = "Email must be valid")
  private String email;

  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;

  private String phone;
  private String address;
  private LocalDate dateOfBirth;
}
