package com.hospital.availability;

import com.hospital.appointment.Appointment;
import com.hospital.appointment.AppointmentRepository;
import com.hospital.appointment.AppointmentStatus;
import com.hospital.availability.dto.AvailabilityCreateRequest;
import com.hospital.availability.dto.AvailabilityResponse;
import com.hospital.availability.dto.AvailableSlotsResponse;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.doctor.Doctor;
import com.hospital.doctor.DoctorRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorAvailabilityService {

  private final DoctorAvailabilityRepository availabilityRepository;
  private final DoctorRepository doctorRepository;
  private final AppointmentRepository appointmentRepository;
  private static final int DEFAULT_SLOT_MINUTES = 30;

  public DoctorAvailabilityService(
      DoctorAvailabilityRepository availabilityRepository,
      DoctorRepository doctorRepository,
      AppointmentRepository appointmentRepository) {
    this.availabilityRepository = availabilityRepository;
    this.doctorRepository = doctorRepository;
    this.appointmentRepository = appointmentRepository;
  }

  public AvailabilityResponse createAvailability(AvailabilityCreateRequest request) {
    Doctor doctor = resolveDoctor(request.getDoctorId());
    Integer slotDuration = request.getSlotDuration() == null ? DEFAULT_SLOT_MINUTES : request.getSlotDuration();
    validateTimeRange(request.getStartTime(), request.getEndTime(), slotDuration);

    boolean overlap = availabilityRepository.existsOverlapping(
        doctor.getId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime());
    if (overlap) {
      throw new IllegalArgumentException("Availability overlaps with existing schedule");
    }

    DoctorAvailability availability = DoctorAvailability.builder()
        .doctor(doctor)
        .dayOfWeek(request.getDayOfWeek())
        .startTime(request.getStartTime())
        .endTime(request.getEndTime())
        .slotDuration(slotDuration)
        .isActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive())
        .build();

    DoctorAvailability saved = availabilityRepository.save(availability);
    return toResponse(saved);
  }

  public List<AvailabilityResponse> getAvailabilityForDoctor(Long doctorId) {
    ensureDoctorExists(doctorId);
    return availabilityRepository.findByDoctorIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId)
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public AvailableSlotsResponse getAvailableSlots(Long doctorId, LocalDate date) {
    ensureDoctorExists(doctorId);
    DayOfWeek dayOfWeek = date.getDayOfWeek();

    List<DoctorAvailability> availabilityList =
        availabilityRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrueOrderByStartTime(doctorId, dayOfWeek);

    if (availabilityList.isEmpty()) {
      return new AvailableSlotsResponse(doctorId, date, List.of());
    }

    Set<LocalTime> bookedTimes = appointmentRepository
        .findByDoctorIdAndAppointmentDateAndStatusIn(doctorId, date, blockingStatuses())
        .stream()
        .map(Appointment::getStartTime)
        .collect(Collectors.toSet());

    Set<LocalTime> slots = new TreeSet<>();
    for (DoctorAvailability availability : availabilityList) {
      slots.addAll(generateSlots(availability, bookedTimes));
    }

    return new AvailableSlotsResponse(doctorId, date, new ArrayList<>(slots));
  }

  public boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime time) {
    try {
      resolveSlotDurationMinutes(doctorId, date, time);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  public int resolveSlotDurationMinutes(Long doctorId, LocalDate date, LocalTime time) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    List<DoctorAvailability> availabilityList =
        availabilityRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrueOrderByStartTime(doctorId, dayOfWeek);

    if (availabilityList.isEmpty()) {
      throw new IllegalArgumentException("Requested time is not within doctor's availability");
    }

    for (DoctorAvailability availability : availabilityList) {
      if (fitsSlot(availability, time)) {
        return availability.getSlotDuration() == null ? DEFAULT_SLOT_MINUTES : availability.getSlotDuration();
      }
    }

    throw new IllegalArgumentException("Requested time is not within doctor's availability");
  }

  private void ensureDoctorExists(Long doctorId) {
    if (!doctorRepository.existsById(doctorId)) {
      throw new ResourceNotFoundException("Doctor not found");
    }
  }

  private void validateTimeRange(LocalTime start, LocalTime end, Integer slotDuration) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start time and end time are required");
    }
    if (!start.isBefore(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    long totalMinutes = Duration.between(start, end).toMinutes();
    if (slotDuration == null || slotDuration <= 0) {
      throw new IllegalArgumentException("Slot duration must be greater than 0");
    }
    if (slotDuration > totalMinutes) {
      throw new IllegalArgumentException("Slot duration exceeds available time range");
    }
    if (totalMinutes % slotDuration != 0) {
      throw new IllegalArgumentException("Time range must be divisible by slot duration");
    }
  }

  private List<LocalTime> generateSlots(DoctorAvailability availability, Set<LocalTime> bookedTimes) {
    List<LocalTime> slots = new ArrayList<>();
    LocalTime current = availability.getStartTime();
    int duration = availability.getSlotDuration() == null ? DEFAULT_SLOT_MINUTES : availability.getSlotDuration();

    while (current.plusMinutes(duration).compareTo(availability.getEndTime()) <= 0) {
      if (!bookedTimes.contains(current)) {
        slots.add(current);
      }
      current = current.plusMinutes(duration);
    }

    return slots;
  }

  private List<AppointmentStatus> blockingStatuses() {
    return List.of(
        AppointmentStatus.REQUESTED,
        AppointmentStatus.ACCEPTED,
        AppointmentStatus.COMPLETED
    );
  }

  private boolean fitsSlot(DoctorAvailability availability, LocalTime time) {
    int duration = availability.getSlotDuration() == null ? DEFAULT_SLOT_MINUTES : availability.getSlotDuration();
    if (time.isBefore(availability.getStartTime())) {
      return false;
    }
    if (time.plusMinutes(duration).isAfter(availability.getEndTime())) {
      return false;
    }

    long minutesFromStart = Duration.between(availability.getStartTime(), time).toMinutes();
    return minutesFromStart % duration == 0;
  }

  private AvailabilityResponse toResponse(DoctorAvailability availability) {
    return new AvailabilityResponse(
        availability.getId(),
        availability.getDoctor().getId(),
        availability.getDayOfWeek(),
        availability.getStartTime(),
        availability.getEndTime(),
        availability.getSlotDuration(),
        availability.getIsActive()
    );
  }

  private Doctor resolveDoctor(Long doctorId) {
    if (doctorId != null) {
      return doctorRepository.findById(doctorId)
          .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    String email = currentUserEmail();
    if (email == null) {
      throw new IllegalArgumentException("Doctor id is required");
    }
    return doctorRepository.findByUserEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
  }

  private String currentUserEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth == null ? null : auth.getName();
  }
}
