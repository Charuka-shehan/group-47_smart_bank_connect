package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.ApprovalRequest;

import java.util.List;

public interface ApprovalService {
    List<ApprovalRequest> getPending();
    List<ApprovalRequest> getAll();
    ApprovalRequest getById(Long id);
    ApprovalRequest approve(Long requestId, String managerEmail, String remarks);
    ApprovalRequest reject(Long requestId, String managerEmail, String remarks);
    ApprovalRequest create(String requestType, Long targetId, String targetReference, String requesterEmail, String remarks);
}
