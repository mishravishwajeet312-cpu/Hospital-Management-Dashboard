package com.hospital.audit.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuditLogResponse {
  private Long id;
  private String action;
  private String entityType;
  private String entityId;
  private String performedBy;
  private Instant createdAt;
  private String details;
}
