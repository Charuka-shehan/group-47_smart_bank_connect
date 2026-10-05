package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
