package com.hospital.prescription;

import com.hospital.medical.MedicalRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "medical_record_id", nullable = false)
  private MedicalRecord medicalRecord;

  @Column(nullable = false, length = 200)
  private String medicineName;

  @Column(nullable = false, length = 100)
  private String dosage;

  @Column(nullable = false, length = 100)
  private String duration;

  @Column(length = 500)
  private String instructions;
}
