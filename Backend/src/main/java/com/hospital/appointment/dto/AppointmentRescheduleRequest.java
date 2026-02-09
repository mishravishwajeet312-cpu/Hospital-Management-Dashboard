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
public class AppointmentRescheduleRequest {
  @NotNull(message = "Appointment date is required")
  private LocalDate appointmentDate;

  @NotNull(message = "Start time is required")
  private LocalTime startTime;

  private LocalTime endTime;
}
