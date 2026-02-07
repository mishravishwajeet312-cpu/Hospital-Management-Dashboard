package com.hospital.availability.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvailableSlotsResponse {
  private Long doctorId;
  private LocalDate date;
  private List<LocalTime> slots;
}
