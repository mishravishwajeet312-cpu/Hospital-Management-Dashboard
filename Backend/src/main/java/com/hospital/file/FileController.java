package com.hospital.file;

import com.hospital.file.dto.FileResponse;
import com.hospital.file.dto.FileUploadResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;

  @PostMapping("/upload")
  public ResponseEntity<FileUploadResponse> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam("medicalRecordId") Long medicalRecordId) {
    return ResponseEntity.ok(fileService.upload(file, medicalRecordId));
  }

  @GetMapping("/{fileId}/download")
  public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
    FileService.FileDownload download = fileService.download(fileId);
    FileEntity file = download.file();
    Path path = download.path();

    Resource resource = new FileSystemResource(path);
    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
    if (file.getFileType() != null) {
      mediaType = MediaType.parseMediaType(file.getFileType());
    }

    return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
        .body(resource);
  }

  @GetMapping("/record/{recordId}")
  public ResponseEntity<List<FileResponse>> listByRecord(@PathVariable Long recordId) {
    return ResponseEntity.ok(fileService.listByMedicalRecord(recordId));
  }
}
