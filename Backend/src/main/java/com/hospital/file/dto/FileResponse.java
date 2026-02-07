package com.hospital.file.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {
  private Long id;
  private String fileName;
  private String fileType;
  private Instant uploadedAt;
  private Long medicalRecordId;
}
