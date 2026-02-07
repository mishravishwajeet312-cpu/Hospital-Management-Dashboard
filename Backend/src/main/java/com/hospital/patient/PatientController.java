package com.hospital.patient;

import com.hospital.common.PagedResponse;
import com.hospital.patient.dto.PatientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

  private final PatientService patientService;

  @GetMapping
  public ResponseEntity<PagedResponse<PatientResponse>> list(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String phone,
      @PageableDefault(size = 10) Pageable pageable) {

    Page<PatientResponse> page = patientService.search(name, phone, pageable);
    return ResponseEntity.ok(new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getTotalElements(),
        page.getTotalPages()
    ));
  }
}
