package com.hospital.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserListResponse {
  private Long id;
  private String name;
  private String email;
  private String role;
}
