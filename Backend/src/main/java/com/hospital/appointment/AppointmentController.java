package com.hospital.appointment;

import com.hospital.appointment.dto.AppointmentBookRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.dto.AppointmentRescheduleRequest;
import com.hospital.appointment.dto.AppointmentStatusUpdateRequest;
import com.hospital.availability.dto.AvailableSlotsResponse;
import com.hospital.common.PagedResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

  private final AppointmentService appointmentService;

  @PostMapping("/book")
  public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody AppointmentBookRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(request));
  }

  @PostMapping("/request")
  public ResponseEntity<AppointmentResponse> request(@Valid @RequestBody AppointmentBookRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(request));
  }

  @GetMapping("/my")
  public ResponseEntity<List<AppointmentResponse>> myAppointments() {
    return ResponseEntity.ok(appointmentService.getMyAppointments());
  }

  @GetMapping("/patient")
  public ResponseEntity<List<AppointmentResponse>> patientAppointments() {
    return ResponseEntity.ok(appointmentService.getMyAppointments());
  }

  @GetMapping("/doctor")
  public ResponseEntity<List<AppointmentResponse>> doctorAppointments() {
    return ResponseEntity.ok(appointmentService.getDoctorAppointments());
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<AppointmentResponse> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody AppointmentStatusUpdateRequest request) {
    return ResponseEntity.ok(appointmentService.updateStatus(id, request));
  }

  @PutMapping("/{id}/accept")
  public ResponseEntity<AppointmentResponse> accept(@PathVariable Long id) {
    return ResponseEntity.ok(appointmentService.accept(id));
  }

  @PutMapping("/{id}/reject")
  public ResponseEntity<AppointmentResponse> reject(@PathVariable Long id) {
    return ResponseEntity.ok(appointmentService.reject(id));
  }

  @PutMapping("/{id}/cancel")
  public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id) {
    return ResponseEntity.ok(appointmentService.cancel(id));
  }

  @PutMapping("/{id}/reschedule")
  public ResponseEntity<AppointmentResponse> reschedule(
      @PathVariable Long id,
      @Valid @RequestBody AppointmentRescheduleRequest request) {
    return ResponseEntity.ok(appointmentService.reschedule(id, request));
  }

  @GetMapping
  public ResponseEntity<PagedResponse<AppointmentResponse>> list(
      @RequestParam(required = false) AppointmentStatus status,
      @RequestParam(required = false) Long doctorId,
      @RequestParam(required = false) Long patientId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @PageableDefault(size = 10) Pageable pageable) {

    Page<AppointmentResponse> page = appointmentService.search(status, doctorId, patientId, date, pageable);
    return ResponseEntity.ok(new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getTotalElements(),
        page.getTotalPages()
    ));
  }

  @GetMapping("/available-slots")
  public ResponseEntity<AvailableSlotsResponse> availableSlots(
      @RequestParam Long doctorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
  }
}
