package com.hospital.file;

import com.hospital.audit.AuditLogService;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.file.dto.FileResponse;
import com.hospital.file.dto.FileUploadResponse;
import com.hospital.medical.MedicalRecord;
import com.hospital.medical.MedicalRecordRepository;
import com.hospital.user.User;
import com.hospital.user.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FileService {

  private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("application/pdf", "image/jpeg", "image/png");
  private static final Set<String> ALLOWED_EXTENSIONS =
      Set.of("pdf", "jpg", "jpeg", "png");

  private final FileRepository fileRepository;
  private final MedicalRecordRepository medicalRecordRepository;
  private final UserRepository userRepository;
  private final AuditLogService auditLogService;

  @Value("${app.file.upload-dir:uploads}")
  private String uploadDir;

  public FileService(
      FileRepository fileRepository,
      MedicalRecordRepository medicalRecordRepository,
      UserRepository userRepository,
      AuditLogService auditLogService) {
    this.fileRepository = fileRepository;
    this.medicalRecordRepository = medicalRecordRepository;
    this.userRepository = userRepository;
    this.auditLogService = auditLogService;
  }

  public FileUploadResponse upload(MultipartFile file, Long medicalRecordId) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is required");
    }

    if (file.getSize() > MAX_SIZE_BYTES) {
      throw new IllegalArgumentException("File size exceeds 5MB limit");
    }

    String originalName = StringUtils.cleanPath(file.getOriginalFilename());
    if (originalName.isBlank() || originalName.contains("..")) {
      throw new IllegalArgumentException("Invalid file name");
    }

    String extension = getExtension(originalName);
    String contentType = file.getContentType();

    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IllegalArgumentException("Invalid file type. Only PDF, JPG, PNG allowed");
    }

    if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("Invalid file content type");
    }

    MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
        .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

    User uploader = currentUser();

    Path storageDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(storageDir);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Could not create upload directory");
    }

    String storedName = UUID.randomUUID() + "." + extension;
    Path targetPath = storageDir.resolve(storedName).normalize();

    try {
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Failed to store file");
    }

    FileEntity entity = FileEntity.builder()
        .fileName(originalName)
        .fileType(contentType == null ? "application/octet-stream" : contentType)
        .filePath(targetPath.toString())
        .uploadedBy(uploader)
        .medicalRecord(record)
        .build();

    FileEntity saved = fileRepository.save(entity);
    auditLogService.log("UPLOAD_FILE", "FILE", saved.getId().toString(),
        "medicalRecordId=" + record.getId());
    return new FileUploadResponse(saved.getId(), saved.getFileName(), saved.getFileType(), saved.getUploadedAt());
  }

  public FileDownload download(Long fileId) {
    FileEntity file = fileRepository.findById(fileId)
        .orElseThrow(() -> new ResourceNotFoundException("File not found"));

    authorizeAccess(file.getMedicalRecord());

    Path path = Paths.get(file.getFilePath());
    if (!Files.exists(path)) {
      throw new ResourceNotFoundException("File not found on server");
    }

    auditLogService.log("DOWNLOAD_FILE", "FILE", file.getId().toString(),
        "medicalRecordId=" + file.getMedicalRecord().getId());
    return new FileDownload(file, path);
  }

  public List<FileResponse> listByMedicalRecord(Long medicalRecordId) {
    MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
        .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

    authorizeAccess(record);

    return fileRepository.findByMedicalRecordId(medicalRecordId)
        .stream()
        .map(file -> new FileResponse(
            file.getId(),
            file.getFileName(),
            file.getFileType(),
            file.getUploadedAt(),
            file.getMedicalRecord().getId()
        ))
        .collect(Collectors.toList());
  }

  private void authorizeAccess(MedicalRecord record) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new AccessDeniedException("Access denied");
    }

    String email = auth.getName();
    boolean isAdmin = hasRole(auth, "ROLE_ADMIN");
    boolean isDoctor = hasRole(auth, "ROLE_DOCTOR");
    boolean isPatient = hasRole(auth, "ROLE_PATIENT");

    if (isAdmin) {
      return;
    }

    if (isDoctor) {
      String doctorEmail = record.getDoctor().getUser().getEmail();
      if (email != null && email.equalsIgnoreCase(doctorEmail)) {
        return;
      }
    }

    if (isPatient) {
      String patientEmail = record.getPatient().getUser().getEmail();
      if (email != null && email.equalsIgnoreCase(patientEmail)) {
        return;
      }
    }

    throw new AccessDeniedException("Access denied");
  }

  private boolean hasRole(Authentication auth, String role) {
    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
  }

  private String getExtension(String filename) {
    int idx = filename.lastIndexOf('.');
    if (idx < 0 || idx == filename.length() - 1) {
      return "";
    }
    return filename.substring(idx + 1).toLowerCase();
  }

  private User currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new AccessDeniedException("Access denied");
    }
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new AccessDeniedException("Access denied"));
  }

  public record FileDownload(FileEntity file, Path path) {}
}
