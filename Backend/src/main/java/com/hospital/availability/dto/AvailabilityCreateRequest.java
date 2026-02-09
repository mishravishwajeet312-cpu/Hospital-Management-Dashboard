package com.hospital.availability.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCreateRequest {

  private Long doctorId;

  @NotNull(message = "Day of week is required")
  private DayOfWeek dayOfWeek;

  @NotNull(message = "Start time is required")
  private LocalTime startTime;

  @NotNull(message = "End time is required")
  private LocalTime endTime;

  private Integer slotDuration;

  private Boolean isActive;
}
