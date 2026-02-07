package com.hospital.doctor;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
  Optional<Doctor> findByUserEmail(String email);
  Optional<Doctor> findByUserId(Long userId);
}
