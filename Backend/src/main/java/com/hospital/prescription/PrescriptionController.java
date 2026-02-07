package com.hospital.prescription;

import com.hospital.prescription.dto.PrescriptionCreateRequest;
import com.hospital.prescription.dto.PrescriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

  private final PrescriptionService prescriptionService;

  @PostMapping
  public ResponseEntity<PrescriptionResponse> create(@Valid @RequestBody PrescriptionCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.create(request));
  }
}
