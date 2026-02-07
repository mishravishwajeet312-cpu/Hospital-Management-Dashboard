package com.hospital.file.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
  private Long id;
  private String fileName;
  private String fileType;
  private Instant uploadedAt;
}
