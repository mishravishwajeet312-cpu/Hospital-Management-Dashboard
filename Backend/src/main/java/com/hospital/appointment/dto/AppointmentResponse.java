package com.hospital.appointment.dto;

import com.hospital.appointment.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentResponse {
  private Long id;
  private Long patientId;
  private Long doctorId;
  private LocalDate appointmentDate;
  private LocalTime appointmentTime;
  private AppointmentStatus status;
}
