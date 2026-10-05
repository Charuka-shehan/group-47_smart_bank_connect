package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.AuditLog;
import com.lankatrust.smartbank.repository.AuditLogRepository;
import com.lankatrust.smartbank.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String actorEmail, String action, String entityType, Long entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .actorEmail(actorEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }
}
