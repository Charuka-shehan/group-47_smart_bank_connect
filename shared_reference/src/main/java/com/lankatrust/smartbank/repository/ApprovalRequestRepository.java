package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByStatusOrderByRequestedAtDesc(String status);
    List<ApprovalRequest> findByRequestTypeAndStatusOrderByRequestedAtDesc(String requestType, String status);
    List<ApprovalRequest> findByRequesterIdOrderByRequestedAtDesc(Long requesterId);
    List<ApprovalRequest> findByTargetId(Long targetId);
    List<ApprovalRequest> findByTargetIdAndRequestTypeIn(Long targetId, java.util.Collection<String> requestTypes);
    void deleteByTargetId(Long targetId);
}
