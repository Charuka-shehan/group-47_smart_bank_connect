package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Account;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AccountService {
    Account submitNewAccount(Long customerId, String accountType, String submittedByEmail);

    Account openAccount(String customerId, String fullName, String email, String phone, String nic,
                        LocalDate dob, String address, String occupation, BigDecimal monthlyIncome,
                        String accountType, BigDecimal initialDeposit, Integer branchId,
                        String nomineeName, String nomineeRelationship,
                        Map<String, MultipartFile> documents,
                        String submittedByEmail);

    Account approveAccount(Long accountId, String approverEmail);
    Account rejectAccount(Long accountId, String approverEmail, String reason);
    Account freezeAccount(Long accountId, String officerEmail);
    Account unfreezeAccount(Long accountId, String officerEmail);
    Account requestFreeze(Long accountId, String officerEmail);
    Account requestUnfreeze(Long accountId, String officerEmail);
    Account requestClosure(Long accountId, String officerEmail);
    Account approveClosure(Long accountId, String approverEmail);
    List<Account> getAll();
    List<Account> getForCustomer(Long customerId);
    Account getById(Long id);
    Account createForCustomer(Long customerId, String accountType);
    com.lankatrust.smartbank.entity.ApprovalRequest requestAccountUpdate(Long accountId, String accountType, Integer branchId,
                                                                         String nomineeName, String nomineeRelationship,
                                                                         String occupation, BigDecimal monthlyIncome, String officerEmail);
    Account applyAccountUpdate(Long accountId, String accountType, Integer branchId,
                               String nomineeName, String nomineeRelationship,
                               String occupation, BigDecimal monthlyIncome);
    void deleteAccountPermanently(Long accountId, String actorEmail);
}
