package com.hospital.audit;

import com.hospital.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(nullable = false, length = 100)
  private String entityType;

  @Column(length = 64)
  private String entityId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "performed_by", nullable = false)
  private User performedBy;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(length = 1000)
  private String details;
}
