package com.hospital.patient;

import com.hospital.user.User;
import org.springframework.data.jpa.domain.Specification;

public final class PatientSpecifications {

  private PatientSpecifications() {
  }

  public static Specification<Patient> nameLike(String name) {
    return (root, query, cb) -> {
      if (name == null || name.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.join("user").get("name")), "%" + name.toLowerCase() + "%");
    };
  }

  public static Specification<Patient> phoneLike(String phone) {
    return (root, query, cb) -> {
      if (phone == null || phone.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.get("phone")), "%" + phone.toLowerCase() + "%");
    };
  }
}
