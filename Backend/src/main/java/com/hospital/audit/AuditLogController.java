package com.hospital.audit;

import com.hospital.audit.dto.AuditLogResponse;
import com.hospital.common.PagedResponse;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

  private final AuditLogRepository auditLogRepository;

  public AuditLogController(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @GetMapping
  public ResponseEntity<PagedResponse<AuditLogResponse>> list(
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) String performedBy,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @PageableDefault(size = 10) Pageable pageable) {

    Specification<AuditLog> spec = Specification.where(AuditLogSpecifications.actionLike(action))
        .and(AuditLogSpecifications.entityTypeLike(entityType))
        .and(AuditLogSpecifications.performedByLike(performedBy))
        .and(AuditLogSpecifications.createdAfter(from))
        .and(AuditLogSpecifications.createdBefore(to));

    Page<AuditLogResponse> page = auditLogRepository.findAll(spec, pageable)
        .map(this::toResponse);

    return ResponseEntity.ok(new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getTotalElements(),
        page.getTotalPages()
    ));
  }

  private AuditLogResponse toResponse(AuditLog log) {
    return new AuditLogResponse(
        log.getId(),
        log.getAction(),
        log.getEntityType(),
        log.getEntityId(),
        log.getPerformedBy().getEmail(),
        log.getCreatedAt(),
        log.getDetails()
    );
  }
}
