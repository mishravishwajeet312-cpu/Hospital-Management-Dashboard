package com.hospital.availability;

import com.hospital.availability.dto.AvailabilityCreateRequest;
import com.hospital.availability.dto.AvailabilityResponse;
import com.hospital.availability.dto.AvailableSlotsResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

  private final DoctorAvailabilityService availabilityService;

  @PostMapping
  public ResponseEntity<AvailabilityResponse> create(@Valid @RequestBody AvailabilityCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.createAvailability(request));
  }

  @GetMapping("/doctor/{doctorId}")
  public ResponseEntity<List<AvailabilityResponse>> getForDoctor(@PathVariable Long doctorId) {
    return ResponseEntity.ok(availabilityService.getAvailabilityForDoctor(doctorId));
  }

  @GetMapping("/doctor/{doctorId}/slots")
  public ResponseEntity<AvailableSlotsResponse> getSlots(
      @PathVariable Long doctorId,
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(availabilityService.getAvailableSlots(doctorId, date));
  }
}
