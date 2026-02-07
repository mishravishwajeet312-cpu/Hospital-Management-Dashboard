package com.hospital.availability.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvailabilityResponse {
  private Long id;
  private Long doctorId;
  private DayOfWeek dayOfWeek;
  private LocalTime startTime;
  private LocalTime endTime;
  private Integer slotDuration;
}
