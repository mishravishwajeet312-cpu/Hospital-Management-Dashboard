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
import java.util.List;
import java.util.stream.Collectors;
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
    Patient patient = resolvePatientForBooking(request);
    Doctor doctor = doctorRepository.findById(request.getDoctorId())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

    if (!availabilityService.isSlotAvailable(doctor.getId(), request.getAppointmentDate(), request.getAppointmentTime())) {
      throw new IllegalArgumentException("Requested time is not within doctor's availability");
    }

    boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
        doctor.getId(), request.getAppointmentDate(), request.getAppointmentTime(), blockingStatuses());
    if (exists) {
      throw new IllegalArgumentException("Time slot is already booked");
    }

    Appointment appointment = Appointment.builder()
        .patient(patient)
        .doctor(doctor)
        .appointmentDate(request.getAppointmentDate())
        .appointmentTime(request.getAppointmentTime())
        .status(AppointmentStatus.PENDING)
        .build();

    Appointment saved = appointmentRepository.save(appointment);
    return toResponse(saved);
  }

  public List<AppointmentResponse> getMyAppointments() {
    Patient patient = patientRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

    return appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId())
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public List<AppointmentResponse> getDoctorAppointments() {
    Doctor doctor = doctorRepository.findByUserEmail(currentUserEmail())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

    return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctor.getId())
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public AppointmentResponse updateStatus(Long id, AppointmentStatusUpdateRequest request) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

    if (!hasRole("ROLE_ADMIN")) {
      Doctor doctor = doctorRepository.findByUserEmail(currentUserEmail())
          .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

      if (!appointment.getDoctor().getId().equals(doctor.getId())) {
        throw new IllegalArgumentException("You are not allowed to update this appointment");
      }
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

    if (appointment.getStatus() == AppointmentStatus.CANCELLED
        || appointment.getStatus() == AppointmentStatus.REJECTED) {
      throw new IllegalArgumentException("Cannot accept a cancelled or rejected appointment");
    }

    boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(
        doctor.getId(),
        appointment.getAppointmentDate(),
        appointment.getAppointmentTime(),
        blockingStatuses(),
        appointment.getId()
    );
    if (exists) {
      throw new IllegalArgumentException("Time slot is already booked");
    }

    appointment.setStatus(AppointmentStatus.ACCEPTED);
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

    if (appointment.getStatus() == AppointmentStatus.CANCELLED
        || appointment.getStatus() == AppointmentStatus.COMPLETED) {
      throw new IllegalArgumentException("Cannot reject a cancelled or completed appointment");
    }

    appointment.setStatus(AppointmentStatus.REJECTED);
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

    if (!availabilityService.isSlotAvailable(
        appointment.getDoctor().getId(), request.getAppointmentDate(), request.getAppointmentTime())) {
      throw new IllegalArgumentException("Requested time is not within doctor's availability");
    }

    boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(
        appointment.getDoctor().getId(),
        request.getAppointmentDate(),
        request.getAppointmentTime(),
        blockingStatuses(),
        appointment.getId()
    );

    if (exists) {
      throw new IllegalArgumentException("Time slot is already booked");
    }

    appointment.setAppointmentDate(request.getAppointmentDate());
    appointment.setAppointmentTime(request.getAppointmentTime());
    appointment.setStatus(AppointmentStatus.PENDING);

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

  private Patient resolvePatientForBooking(AppointmentBookRequest request) {
    if (hasRole("ROLE_PATIENT")) {
      return patientRepository.findByUserEmail(currentUserEmail())
          .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    if (hasRole("ROLE_RECEPTIONIST") || hasRole("ROLE_ADMIN")) {
      if (request.getPatientId() == null) {
        throw new IllegalArgumentException("Patient id is required for receptionist/admin booking");
      }
      return patientRepository.findById(request.getPatientId())
          .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    throw new IllegalArgumentException("You are not allowed to book appointments");
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
        appointment.getAppointmentTime(),
        appointment.getStatus()
    );
  }

  private List<AppointmentStatus> blockingStatuses() {
    return List.of(
        AppointmentStatus.PENDING,
        AppointmentStatus.ACCEPTED,
        AppointmentStatus.COMPLETED
    );
  }
}
