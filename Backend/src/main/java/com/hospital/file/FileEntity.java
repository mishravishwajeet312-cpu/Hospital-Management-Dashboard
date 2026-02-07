package com.hospital.file;

import com.hospital.medical.MedicalRecord;
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
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 100)
  private String fileType;

  @Column(nullable = false, length = 500)
  private String filePath;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant uploadedAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "uploaded_by", nullable = false)
  private User uploadedBy;

  @ManyToOne(optional = false)
  @JoinColumn(name = "medical_record_id", nullable = false)
  private MedicalRecord medicalRecord;
}
