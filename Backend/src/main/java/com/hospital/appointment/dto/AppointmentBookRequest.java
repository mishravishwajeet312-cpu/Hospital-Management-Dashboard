package com.hospital.appointment.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentBookRequest {
  @NotNull(message = "Doctor id is required")
  private Long doctorId;

  private Long patientId;

  @NotNull(message = "Appointment date is required")
  private LocalDate appointmentDate;

  @NotNull(message = "Appointment time is required")
  private LocalTime appointmentTime;
}
