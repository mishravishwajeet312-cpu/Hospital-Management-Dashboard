package com.hospital.audit;

import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

  private AuditLogSpecifications() {
  }

  public static Specification<AuditLog> actionLike(String action) {
    return (root, query, cb) -> {
      if (action == null || action.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.get("action")), "%" + action.toLowerCase() + "%");
    };
  }

  public static Specification<AuditLog> entityTypeLike(String entityType) {
    return (root, query, cb) -> {
      if (entityType == null || entityType.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.get("entityType")), "%" + entityType.toLowerCase() + "%");
    };
  }

  public static Specification<AuditLog> performedByLike(String performedBy) {
    return (root, query, cb) -> {
      if (performedBy == null || performedBy.isBlank()) {
        return null;
      }
      return cb.like(cb.lower(root.join("performedBy").get("email")), "%" + performedBy.toLowerCase() + "%");
    };
  }

  public static Specification<AuditLog> createdAfter(Instant from) {
    return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<AuditLog> createdBefore(Instant to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }
}
