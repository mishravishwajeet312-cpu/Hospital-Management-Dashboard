package com.hospital.availability;

import com.hospital.availability.dto.AvailabilityCreateRequest;
import com.hospital.availability.dto.AvailabilityResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorAvailabilityDoctorController {

  private final DoctorAvailabilityService availabilityService;

  @PostMapping("/availability")
  public ResponseEntity<AvailabilityResponse> create(@Valid @RequestBody AvailabilityCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.createAvailability(request));
  }

  @GetMapping("/{doctorId}/availability")
  public ResponseEntity<List<AvailabilityResponse>> getForDoctor(@PathVariable Long doctorId) {
    return ResponseEntity.ok(availabilityService.getAvailabilityForDoctor(doctorId));
  }
}
