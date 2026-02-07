package com.hospital.audit;

import com.hospital.user.User;
import com.hospital.user.UserRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;

  public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
    this.auditLogRepository = auditLogRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public void log(String action, String entityType, String entityId, String details) {
    User actor = currentUser();
    if (actor == null) {
      return;
    }

    AuditLog log = AuditLog.builder()
        .action(action)
        .entityType(entityType)
        .entityId(entityId)
        .performedBy(actor)
        .details(details)
        .build();

    auditLogRepository.save(log);
  }

  private User currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
      return null;
    }

    Optional<User> user = userRepository.findByEmail(auth.getName());
    return user.orElse(null);
  }
}
