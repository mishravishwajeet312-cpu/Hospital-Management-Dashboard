package com.hospital.file;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
  List<FileEntity> findByMedicalRecordIdIn(List<Long> medicalRecordIds);

  void deleteByMedicalRecordIdIn(List<Long> medicalRecordIds);

  List<FileEntity> findByMedicalRecordId(Long medicalRecordId);
}
