package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.CommunicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunicationLogRepository extends JpaRepository<CommunicationLog, Long> {
    List<CommunicationLog> findAllByOrderByCreatedAtDesc();
    List<CommunicationLog> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
