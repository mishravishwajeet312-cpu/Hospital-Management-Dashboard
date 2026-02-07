package com.hospital.doctor;

import com.hospital.doctor.dto.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorService {

  private final DoctorRepository doctorRepository;

  public DoctorService(DoctorRepository doctorRepository) {
    this.doctorRepository = doctorRepository;
  }

  public Page<DoctorResponse> search(String specialization, Pageable pageable) {
    Specification<Doctor> spec = Specification.where(DoctorSpecifications.specializationLike(specialization));
    return doctorRepository.findAll(spec, pageable).map(this::toResponse);
  }

  private DoctorResponse toResponse(Doctor doctor) {
    return new DoctorResponse(
        doctor.getId(),
        doctor.getUser().getId(),
        doctor.getUser().getName(),
        doctor.getUser().getEmail(),
        doctor.getSpecialization(),
        doctor.getPhone()
    );
  }
}
