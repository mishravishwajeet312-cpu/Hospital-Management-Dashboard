package com.hospital.medical;

import com.hospital.doctor.Doctor;
import com.hospital.patient.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @ManyToOne(optional = false)
  @JoinColumn(name = "doctor_id", nullable = false)
  private Doctor doctor;

  @Column(nullable = false, length = 500)
  private String diagnosis;

  @Column(length = 2000)
  private String notes;

  @Column(nullable = false)
  private LocalDate visitDate;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
