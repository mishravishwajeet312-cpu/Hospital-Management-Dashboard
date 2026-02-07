package com.hospital.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserResponse {
  private Long userId;
  private Long profileId;
  private String name;
  private String email;
  private String role;
}
