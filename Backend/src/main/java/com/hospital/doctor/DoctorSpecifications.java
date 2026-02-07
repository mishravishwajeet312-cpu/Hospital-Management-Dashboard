package com.hospital.doctor;

import org.springframework.data.jpa.domain.Specification;

public final class DoctorSpecifications {

  private DoctorSpecifications() {
  }

  public static Specification<Doctor> specializationLike(String specialization) {
    return (root, query, cb) -> {
      if (specialization == null || specialization.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.get("specialization")), "%" + specialization.toLowerCase() + "%");
    };
  }
}
