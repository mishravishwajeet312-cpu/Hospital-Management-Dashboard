package com.hospital.user;

import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

  private UserSpecifications() {
  }

  public static Specification<User> nameLike(String name) {
    return (root, query, cb) -> {
      if (name == null || name.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    };
  }

  public static Specification<User> emailLike(String email) {
    return (root, query, cb) -> {
      if (email == null || email.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    };
  }

  public static Specification<User> hasRole(Role role) {
    return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
  }
}
