package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.TransactionCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCorrectionRepository extends JpaRepository<TransactionCorrection, Long> {
    List<TransactionCorrection> findByStatusOrderByCreatedAtDesc(String status);
    List<TransactionCorrection> findAllByOrderByCreatedAtDesc();
    List<TransactionCorrection> findByTransactionId(Long transactionId);
    void deleteByTransactionId(Long transactionId);
}
