package com.lankatrust.smartbank.service;

public interface AuditLogService {
    void log(String actorEmail, String action, String entityType, Long entityId, String details);
}
