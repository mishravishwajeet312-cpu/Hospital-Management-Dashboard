package com.hospital.config;

import com.hospital.user.Role;
import com.hospital.user.User;
import com.hospital.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfig {

  @Value("${app.admin.bootstrap-enabled:true}")
  private boolean bootstrapEnabled;

  @Value("${app.admin.email:admin@hospital.com}")
  private String adminEmail;

  @Value("${app.admin.password:Admin@123}")
  private String adminPassword;

  @Value("${app.admin.name:System Admin}")
  private String adminName;

  @Bean
  public CommandLineRunner adminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      if (!bootstrapEnabled) {
        return;
      }

      boolean exists = userRepository.findByEmail(adminEmail).isPresent();
      if (exists) {
        return;
      }

      User admin = User.builder()
          .name(adminName)
          .email(adminEmail)
          .password(passwordEncoder.encode(adminPassword))
          .role(Role.ADMIN)
          .build();

      userRepository.save(admin);
    };
  }
}
