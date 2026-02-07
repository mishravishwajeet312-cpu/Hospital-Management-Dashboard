package com.hospital.medical;

import com.hospital.common.PagedResponse;
import com.hospital.medical.dto.MedicalRecordCreateRequest;
import com.hospital.medical.dto.MedicalRecordResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

  private final MedicalRecordService medicalRecordService;

  @PostMapping
  public ResponseEntity<MedicalRecordResponse> create(@Valid @RequestBody MedicalRecordCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecordService.create(request));
  }

  @GetMapping("/patient/{patientId}")
  public ResponseEntity<List<MedicalRecordResponse>> getForPatient(@PathVariable Long patientId) {
    return ResponseEntity.ok(medicalRecordService.getForPatient(patientId));
  }

  @GetMapping("/my")
  public ResponseEntity<List<MedicalRecordResponse>> getMyRecords() {
    return ResponseEntity.ok(medicalRecordService.getMyRecords());
  }

  @GetMapping
  public ResponseEntity<PagedResponse<MedicalRecordResponse>> list(
      @PageableDefault(size = 10) Pageable pageable) {

    Page<MedicalRecordResponse> page = medicalRecordService.getAll(pageable);
    return ResponseEntity.ok(new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getTotalElements(),
        page.getTotalPages()
    ));
  }
}
