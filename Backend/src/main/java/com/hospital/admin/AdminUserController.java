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
import com.hospital.common.PagedResponse;
import com.hospital.user.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final AdminUserService adminUserService;

  @PostMapping("/admin")
  public ResponseEntity<AdminUserResponse> createAdmin(@Valid @RequestBody AdminCreateAdminRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createAdmin(request));
  }

  @PostMapping("/receptionist")
  public ResponseEntity<AdminUserResponse> createReceptionist(
      @Valid @RequestBody AdminCreateReceptionistRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createReceptionist(request));
  }

  @PostMapping("/doctor")
  public ResponseEntity<AdminUserResponse> createDoctor(@Valid @RequestBody AdminCreateDoctorRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createDoctor(request));
  }

  @PostMapping("/patient")
  public ResponseEntity<AdminUserResponse> createPatient(@Valid @RequestBody AdminCreatePatientRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createPatient(request));
  }

  @PutMapping("/admin/{userId}")
  public ResponseEntity<AdminUserResponse> updateAdmin(
      @PathVariable Long userId,
      @Valid @RequestBody AdminUpdateUserRequest request) {
    return ResponseEntity.ok(adminUserService.updateAdmin(userId, request));
  }

  @PutMapping("/receptionist/{userId}")
  public ResponseEntity<AdminUserResponse> updateReceptionist(
      @PathVariable Long userId,
      @Valid @RequestBody AdminUpdateUserRequest request) {
    return ResponseEntity.ok(adminUserService.updateReceptionist(userId, request));
  }

  @PutMapping("/doctor/{userId}")
  public ResponseEntity<AdminUserResponse> updateDoctor(
      @PathVariable Long userId,
      @Valid @RequestBody AdminUpdateDoctorRequest request) {
    return ResponseEntity.ok(adminUserService.updateDoctor(userId, request));
  }

  @PutMapping("/patient/{userId}")
  public ResponseEntity<AdminUserResponse> updatePatient(
      @PathVariable Long userId,
      @Valid @RequestBody AdminUpdatePatientRequest request) {
    return ResponseEntity.ok(adminUserService.updatePatient(userId, request));
  }

  @GetMapping
  public ResponseEntity<PagedResponse<AdminUserListResponse>> listUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) Role role,
      @PageableDefault(size = 10) Pageable pageable) {

    Page<AdminUserListResponse> page = adminUserService.listUsers(name, email, role, pageable);
    return ResponseEntity.ok(new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getTotalElements(),
        page.getTotalPages()
    ));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
    adminUserService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }
}
