package com.hospital.appointment;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class AppointmentSpecifications {

  private AppointmentSpecifications() {
  }

  public static Specification<Appointment> hasStatus(AppointmentStatus status) {
    return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
  }

  public static Specification<Appointment> hasDoctorId(Long doctorId) {
    return (root, query, cb) -> doctorId == null ? null : cb.equal(root.get("doctor").get("id"), doctorId);
  }

  public static Specification<Appointment> hasPatientId(Long patientId) {
    return (root, query, cb) -> patientId == null ? null : cb.equal(root.get("patient").get("id"), patientId);
  }

  public static Specification<Appointment> hasDate(LocalDate date) {
    return (root, query, cb) -> date == null ? null : cb.equal(root.get("appointmentDate"), date);
  }
}
