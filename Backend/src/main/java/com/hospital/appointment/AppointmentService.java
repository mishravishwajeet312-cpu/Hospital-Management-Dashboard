package com.hospital.appointment;

import com.hospital.appointment.dto.AppointmentBookRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.dto.AppointmentRescheduleRequest;
import com.hospital.appointment.dto.AppointmentStatusUpdateRequest;
import com.hospital.availability.DoctorAvailabilityService;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.doctor.Doctor;
import com.hospital.doctor.DoctorRepository;
import com.hospital.patient.Patient;
import com.hospital.patient.PatientRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentService {

  private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
  private final AppointmentRepository appointmentRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final DoctorAvailabilityService availabilityService;

  public AppointmentService(
      AppointmentRepository appointmentRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      DoctorAvailabilityService availabilityService) {
    this.appointmentRepository = appointmentRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.availabilityService = availabilityService;
  }

  public AppointmentResponse book(AppointmentBookRequest request) {
    Patient patient = resolvePatientForBooking();
    Doctor doctor = doctorRepository.findById(request.getDoctorId())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

    LocalTime startTime = request.getStartTime();
    int slotMinutes =
        availabilityService.resolveSlotDurationMinutes(doctor.getId(), request.getAppointmentDate(), startTime);
    LocalTime endTime = resolveEndTime(startTime, request.getEndTime(), slotMinutes);

    boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
        doctor.getId(), request.getAppointmentDate(), startTime, blockingStatuses());
    if (exists) {
      throw new IllegalArgumentException("Time slot is already booked");
    }

    Appointment appointment = Appointment.builder()
        .patient(patient)
        .doctor(doctor)
        .appointmentDate(request.getAppointmentDate())
        .startTime(startTime)
        .endTime(endTime)
        .status(AppointmentStatus.REQUESTED)
        .reason(request.getReason())
        .build();

    Appointment saved = appointmentRepository.save(appointment);
    log.info("Appointment requested: id={}, doctorId={}, patientId={}, date={}, startTime={}",
        saved.getId(), doctor.getId(), patient.getId(), saved.getAppointmentDate(), saved.getStartTime());
    return toResponse(saved);
  }

  public List<AppointmentResponse> getMyAppointments() {
    Patient patient = patientRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

    return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.getId())
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public List<AppointmentResponse> getDoctorAppointments() {
    Doctor doctor = doctorRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

    return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctor.getId())
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public AppointmentResponse updateStatus(Long id, AppointmentStatusUpdateRequest request) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

    if (!hasRole("ROLE_ADMIN")) {
      throw new IllegalArgumentException("Only admins can update appointment status");
    }

    appointment.setStatus(request.getStatus());
    return toResponse(appointment);
  }

  public AppointmentResponse accept(Long id) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

    Doctor doctor = doctorRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

    if (!appointment.getDoctor().getId().equals(doctor.getId())) {
      throw new IllegalArgumentException("You are not allowed to accept this appointment");
    }

    if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
      throw new IllegalArgumentException("Only requested appointments can be accepted");
    }

    boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusInAndIdNot(
        doctor.getId(),
        appointment.getAppointmentDate(),
        appointment.getStartTime(),
        blockingStatuses(),
        appointment.getId()
    );
    if (exists) {
      throw new IllegalArgumentException("Time slot is already booked");
    }

    appointment.setStatus(AppointmentStatus.ACCEPTED);
    log.info("Appointment accepted: id={}, doctorId={}", appointment.getId(), doctor.getId());
    return toResponse(appointment);
  }

  public AppointmentResponse reject(Long id) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

    Doctor doctor = doctorRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

    if (!appointment.getDoctor().getId().equals(doctor.getId())) {
      throw new IllegalArgumentException("You are not allowed to reject this appointment");
    }

    if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
      throw new IllegalArgumentException("Only requested appointments can be rejected");
    }

    appointment.setStatus(AppointmentStatus.REJECTED);
    log.info("Appointment rejected: id={}, doctorId={}", appointment.getId(), doctor.getId());
    return toResponse(appointment);
  }

  public AppointmentResponse cancel(Long id) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

    if (hasRole("ROLE_PATIENT")) {
      Patient patient = patientRepository.findByUserEmail(currentUserEmail())
          .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
      if (!appointment.getPatient().getId().equals(patient.getId())) {
        throw new IllegalArgumentException("You are not allowed to cancel this appointment");
      }
    }

    appointment.setStatus(AppointmentStatus.CANCELLED);
    return toResponse(appointment);
  }

  public AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

    if (hasRole("ROLE_PATIENT")) {
      Patient patient = patientRepository.findByUserEmail(currentUserEmail())
          .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
      if (!appointment.getPatient().getId().equals(patient.getId())) {
        throw new IllegalArgumentException("You are not allowed to reschedule this appointment");
      }
    }

    LocalTime startTime = request.getStartTime();
    int slotMinutes = availabilityService.resolveSlotDurationMinutes(
        appointment.getDoctor().getId(), request.getAppointmentDate(), startTime);
    LocalTime endTime = resolveEndTime(startTime, request.getEndTime(), slotMinutes);

    boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusInAndIdNot(
        appointment.getDoctor().getId(),
        request.getAppointmentDate(),
        startTime,
        blockingStatuses(),
        appointment.getId()
    );

    if (exists) {
      throw new IllegalArgumentException("Time slot is already booked");
    }

    appointment.setAppointmentDate(request.getAppointmentDate());
    appointment.setStartTime(startTime);
    appointment.setEndTime(endTime);
    appointment.setStatus(AppointmentStatus.REQUESTED);

    return toResponse(appointment);
  }

  public Page<AppointmentResponse> search(AppointmentStatus status, Long doctorId, Long patientId, LocalDate date,
      Pageable pageable) {
    Specification<Appointment> spec = Specification.where(AppointmentSpecifications.hasStatus(status))
        .and(AppointmentSpecifications.hasDoctorId(doctorId))
        .and(AppointmentSpecifications.hasPatientId(patientId))
        .and(AppointmentSpecifications.hasDate(date));

    return appointmentRepository.findAll(spec, pageable).map(this::toResponse);
  }

  public com.hospital.availability.dto.AvailableSlotsResponse getAvailableSlots(Long doctorId, LocalDate date) {
    return availabilityService.getAvailableSlots(doctorId, date);
  }

  private Patient resolvePatientForBooking() {
    if (!hasRole("ROLE_PATIENT")) {
      throw new IllegalArgumentException("Only patients can request appointments");
    }
    return patientRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
  }

  private String currentUserEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth == null ? null : auth.getName();
  }

  private boolean hasRole(String role) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
  }

  private AppointmentResponse toResponse(Appointment appointment) {
    return new AppointmentResponse(
        appointment.getId(),
        appointment.getPatient().getId(),
        appointment.getDoctor().getId(),
        appointment.getAppointmentDate(),
        appointment.getStartTime(),
        appointment.getEndTime(),
        appointment.getStatus(),
        appointment.getReason()
    );
  }

  private List<AppointmentStatus> blockingStatuses() {
    return List.of(
        AppointmentStatus.REQUESTED,
        AppointmentStatus.ACCEPTED,
        AppointmentStatus.COMPLETED
    );
  }

  private LocalTime resolveEndTime(LocalTime startTime, LocalTime endTime, int slotMinutes) {
    if (startTime == null) {
      throw new IllegalArgumentException("Start time is required");
    }
    LocalTime computed = startTime.plusMinutes(slotMinutes);
    if (endTime == null) {
      return computed;
    }
    if (!endTime.equals(computed)) {
      throw new IllegalArgumentException("End time must match slot duration");
    }
    return endTime;
  }
}
