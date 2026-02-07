package com.hospital.admin;

import com.hospital.admin.dto.AdminCreateAdminRequest;
import com.hospital.admin.dto.AdminCreateDoctorRequest;
import com.hospital.admin.dto.AdminCreatePatientRequest;
import com.hospital.admin.dto.AdminCreateReceptionistRequest;
import com.hospital.admin.dto.AdminUpdateDoctorRequest;
import com.hospital.admin.dto.AdminUpdatePatientRequest;
import com.hospital.admin.dto.AdminUpdateUserRequest;
import com.hospital.admin.dto.AdminUserListResponse;
import com.hospital.admin.dto.AdminUserResponse;
import com.hospital.appointment.AppointmentRepository;
import com.hospital.audit.AuditLogService;
import com.hospital.availability.DoctorAvailabilityRepository;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.common.exception.UserAlreadyExistsException;
import com.hospital.doctor.Doctor;
import com.hospital.doctor.DoctorRepository;
import com.hospital.file.FileEntity;
import com.hospital.file.FileRepository;
import com.hospital.medical.MedicalRecordRepository;
import com.hospital.patient.Patient;
import com.hospital.patient.PatientRepository;
import com.hospital.prescription.PrescriptionRepository;
import com.hospital.user.Role;
import com.hospital.user.User;
import com.hospital.user.UserRepository;
import com.hospital.user.UserSpecifications;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminUserService {

  private final UserRepository userRepository;
  private final DoctorRepository doctorRepository;
  private final PatientRepository patientRepository;
  private final AppointmentRepository appointmentRepository;
  private final DoctorAvailabilityRepository availabilityRepository;
  private final MedicalRecordRepository medicalRecordRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final FileRepository fileRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuditLogService auditLogService;

  public AdminUserService(
      UserRepository userRepository,
      DoctorRepository doctorRepository,
      PatientRepository patientRepository,
      AppointmentRepository appointmentRepository,
      DoctorAvailabilityRepository availabilityRepository,
      MedicalRecordRepository medicalRecordRepository,
      PrescriptionRepository prescriptionRepository,
      FileRepository fileRepository,
      PasswordEncoder passwordEncoder,
      AuditLogService auditLogService) {
    this.userRepository = userRepository;
    this.doctorRepository = doctorRepository;
    this.patientRepository = patientRepository;
    this.appointmentRepository = appointmentRepository;
    this.availabilityRepository = availabilityRepository;
    this.medicalRecordRepository = medicalRecordRepository;
    this.prescriptionRepository = prescriptionRepository;
    this.fileRepository = fileRepository;
    this.passwordEncoder = passwordEncoder;
    this.auditLogService = auditLogService;
  }

  public AdminUserResponse createAdmin(AdminCreateAdminRequest request) {
    User user = createUser(request.getName(), request.getEmail(), request.getPassword(), Role.ADMIN);
    auditLogService.log("CREATE_USER", "USER", user.getId().toString(), "role=ADMIN");
    return new AdminUserResponse(user.getId(), null, user.getName(), user.getEmail(), user.getRole().name());
  }

  public AdminUserResponse createReceptionist(AdminCreateReceptionistRequest request) {
    User user = createUser(request.getName(), request.getEmail(), request.getPassword(), Role.RECEPTIONIST);
    auditLogService.log("CREATE_USER", "USER", user.getId().toString(), "role=RECEPTIONIST");
    return new AdminUserResponse(user.getId(), null, user.getName(), user.getEmail(), user.getRole().name());
  }

  public AdminUserResponse createDoctor(AdminCreateDoctorRequest request) {
    User user = createUser(request.getName(), request.getEmail(), request.getPassword(), Role.DOCTOR);

    Doctor doctor = Doctor.builder()
        .user(user)
        .specialization(request.getSpecialization())
        .phone(request.getPhone())
        .build();

    Doctor saved = doctorRepository.save(doctor);
    auditLogService.log("CREATE_DOCTOR", "DOCTOR", saved.getId().toString(), "userId=" + user.getId());
    return new AdminUserResponse(user.getId(), saved.getId(), user.getName(), user.getEmail(), user.getRole().name());
  }

  public AdminUserResponse createPatient(AdminCreatePatientRequest request) {
    User user = createUser(request.getName(), request.getEmail(), request.getPassword(), Role.PATIENT);

    Patient patient = Patient.builder()
        .user(user)
        .phone(request.getPhone())
        .address(request.getAddress())
        .dateOfBirth(request.getDateOfBirth())
        .build();

    Patient saved = patientRepository.save(patient);
    auditLogService.log("CREATE_PATIENT", "PATIENT", saved.getId().toString(), "userId=" + user.getId());
    return new AdminUserResponse(user.getId(), saved.getId(), user.getName(), user.getEmail(), user.getRole().name());
  }

  public AdminUserResponse updateAdmin(Long userId, AdminUpdateUserRequest request) {
    User user = getUserByRole(userId, Role.ADMIN);
    applyUserUpdates(user, request);
    auditLogService.log("UPDATE_USER", "USER", user.getId().toString(), "role=ADMIN");
    return new AdminUserResponse(user.getId(), null, user.getName(), user.getEmail(), user.getRole().name());
  }

  public AdminUserResponse updateReceptionist(Long userId, AdminUpdateUserRequest request) {
    User user = getUserByRole(userId, Role.RECEPTIONIST);
    applyUserUpdates(user, request);
    auditLogService.log("UPDATE_USER", "USER", user.getId().toString(), "role=RECEPTIONIST");
    return new AdminUserResponse(user.getId(), null, user.getName(), user.getEmail(), user.getRole().name());
  }

  public AdminUserResponse updateDoctor(Long userId, AdminUpdateDoctorRequest request) {
    User user = getUserByRole(userId, Role.DOCTOR);
    applyUserUpdates(user, new AdminUpdateUserRequest(request.getName(), request.getEmail(), request.getPassword()));

    Doctor doctor = doctorRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

    if (request.getSpecialization() != null) {
      doctor.setSpecialization(request.getSpecialization());
    }
    if (request.getPhone() != null) {
      doctor.setPhone(request.getPhone());
    }

    Doctor saved = doctorRepository.save(doctor);
    auditLogService.log("UPDATE_DOCTOR", "DOCTOR", saved.getId().toString(), "userId=" + user.getId());
    return new AdminUserResponse(user.getId(), saved.getId(), user.getName(), user.getEmail(), user.getRole().name());
  }

  public AdminUserResponse updatePatient(Long userId, AdminUpdatePatientRequest request) {
    User user = getUserByRole(userId, Role.PATIENT);
    applyUserUpdates(user, new AdminUpdateUserRequest(request.getName(), request.getEmail(), request.getPassword()));

    Patient patient = patientRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

    if (request.getPhone() != null) {
      patient.setPhone(request.getPhone());
    }
    if (request.getAddress() != null) {
      patient.setAddress(request.getAddress());
    }
    if (request.getDateOfBirth() != null) {
      patient.setDateOfBirth(request.getDateOfBirth());
    }

    Patient saved = patientRepository.save(patient);
    auditLogService.log("UPDATE_PATIENT", "PATIENT", saved.getId().toString(), "userId=" + user.getId());
    return new AdminUserResponse(user.getId(), saved.getId(), user.getName(), user.getEmail(), user.getRole().name());
  }

  public Page<AdminUserListResponse> listUsers(String name, String email, Role role, Pageable pageable) {
    Specification<User> spec = Specification.where(UserSpecifications.nameLike(name))
        .and(UserSpecifications.emailLike(email))
        .and(UserSpecifications.hasRole(role));

    return userRepository.findAll(spec, pageable)
        .map(user -> new AdminUserListResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
  }

  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    try {
      if (user.getRole() == Role.DOCTOR) {
        deleteDoctorCascade(userId);
      } else if (user.getRole() == Role.PATIENT) {
        deletePatientCascade(userId);
      }

      userRepository.delete(user);
      auditLogService.log("DELETE_USER", "USER", userId.toString(), "role=" + user.getRole().name());
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalArgumentException("Cannot delete user with related records");
    }
  }

  private void deleteDoctorCascade(Long userId) {
    Doctor doctor = doctorRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

    Long doctorId = doctor.getId();
    List<Long> recordIds = medicalRecordRepository.findIdsByDoctorId(doctorId);
    deleteRecordsWithFiles(recordIds);

    appointmentRepository.deleteByDoctorId(doctorId);
    availabilityRepository.deleteByDoctorId(doctorId);
    doctorRepository.delete(doctor);
  }

  private void deletePatientCascade(Long userId) {
    Patient patient = patientRepository.findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

    Long patientId = patient.getId();
    List<Long> recordIds = medicalRecordRepository.findIdsByPatientId(patientId);
    deleteRecordsWithFiles(recordIds);

    appointmentRepository.deleteByPatientId(patientId);
    patientRepository.delete(patient);
  }

  private void deleteRecordsWithFiles(List<Long> recordIds) {
    if (recordIds == null || recordIds.isEmpty()) {
      return;
    }

    List<FileEntity> files = fileRepository.findByMedicalRecordIdIn(recordIds);
    for (FileEntity file : files) {
      try {
        Files.deleteIfExists(Path.of(file.getFilePath()));
      } catch (Exception ignored) {
      }
    }

    fileRepository.deleteByMedicalRecordIdIn(recordIds);
    prescriptionRepository.deleteByMedicalRecordIdIn(recordIds);
    medicalRecordRepository.deleteAllById(recordIds);
  }

  private User createUser(String name, String email, String password, Role role) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new UserAlreadyExistsException("User already exists");
    }

    User user = User.builder()
        .name(name)
        .email(email)
        .password(passwordEncoder.encode(password))
        .role(role)
        .build();

    return userRepository.save(user);
  }

  private User getUserByRole(Long userId, Role role) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getRole() != role) {
      throw new IllegalArgumentException("User role mismatch");
    }

    return user;
  }

  private void applyUserUpdates(User user, AdminUpdateUserRequest request) {
    if (request.getName() != null && !request.getName().isBlank()) {
      user.setName(request.getName());
    }

    if (request.getEmail() != null && !request.getEmail().isBlank()) {
      boolean changed = !request.getEmail().equalsIgnoreCase(user.getEmail());
      if (changed && userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new UserAlreadyExistsException("User already exists");
      }
      user.setEmail(request.getEmail());
    }

    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    userRepository.save(user);
  }
}
