package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.StatementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatementRequestRepository extends JpaRepository<StatementRequest, Long> {
    List<StatementRequest> findAllByOrderByCreatedAtDesc();
    List<StatementRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<StatementRequest> findByAccountId(Long accountId);
    void deleteByAccountId(Long accountId);
}
