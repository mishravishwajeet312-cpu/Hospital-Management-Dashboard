package com.hospital.availability.dto;

import jakarta.validation.constraints.Min;
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

  @NotNull(message = "Doctor id is required")
  private Long doctorId;

  @NotNull(message = "Day of week is required")
  private DayOfWeek dayOfWeek;

  @NotNull(message = "Start time is required")
  private LocalTime startTime;

  @NotNull(message = "End time is required")
  private LocalTime endTime;

  @NotNull(message = "Slot duration is required")
  @Min(value = 1, message = "Slot duration must be at least 1 minute")
  private Integer slotDuration;
}
