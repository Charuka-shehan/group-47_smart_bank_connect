package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.Loan;
import com.lankatrust.smartbank.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomerId(Long customerId);
    List<Loan> findByStatus(LoanStatus status);
}
