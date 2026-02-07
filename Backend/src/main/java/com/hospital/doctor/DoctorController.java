package com.hospital.doctor;

import com.hospital.common.PagedResponse;
import com.hospital.doctor.dto.DoctorResponse;
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
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

  private final DoctorService doctorService;

  @GetMapping
  public ResponseEntity<PagedResponse<DoctorResponse>> list(
      @RequestParam(required = false) String specialization,
      @PageableDefault(size = 10) Pageable pageable) {

    Page<DoctorResponse> page = doctorService.search(specialization, pageable);
    return ResponseEntity.ok(new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getTotalElements(),
        page.getTotalPages()
    ));
  }
}
