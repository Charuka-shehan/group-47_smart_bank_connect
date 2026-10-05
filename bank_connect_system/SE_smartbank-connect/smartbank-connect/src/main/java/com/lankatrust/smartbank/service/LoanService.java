package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Loan;

import java.math.BigDecimal;
import java.util.List;

public interface LoanService {
    Loan submitApplication(Long customerId, String loanType, BigDecimal amount, BigDecimal monthlyIncome,
                            String employmentStatus, String documentsSummary);
    Loan verify(Long loanId, String officerEmail, String remarks);
    Loan complianceCheck(Long loanId, String complianceOfficerEmail, String remarks);
    Loan approve(Long loanId, String managerEmail);
    Loan reject(Long loanId, String reviewerEmail, String reason);
    Loan cancel(Long loanId, Long customerId);
    Loan updateApplication(Long loanId, Long customerId, String loanType, BigDecimal amount,
                           BigDecimal monthlyIncome, String employmentStatus, String documentsSummary);
    List<Loan> getForCustomer(Long customerId);
    List<Loan> getAll();
    Loan getById(Long id);
}
