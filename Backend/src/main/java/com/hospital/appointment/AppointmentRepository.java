package com.hospital.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {
  boolean existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
      Long doctorId, LocalDate date, LocalTime time, Collection<AppointmentStatus> statuses);

  boolean existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusInAndIdNot(
      Long doctorId, LocalDate date, LocalTime time, Collection<AppointmentStatus> statuses, Long id);

  List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

  List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

  List<Appointment> findByDoctorIdAndAppointmentDateAndStatusIn(
      Long doctorId, LocalDate date, Collection<AppointmentStatus> statuses);

  void deleteByDoctorId(Long doctorId);

  void deleteByPatientId(Long patientId);
}
