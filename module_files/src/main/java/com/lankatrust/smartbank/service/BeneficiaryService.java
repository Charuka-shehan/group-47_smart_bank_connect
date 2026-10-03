package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Beneficiary;

import java.util.List;

public interface BeneficiaryService {
    Beneficiary addBeneficiary(Long userId, String beneficiaryName, String accountNumber, String bank, String nickname);
    List<Beneficiary> getForUser(Long userId);
    void deleteBeneficiary(Long beneficiaryId);
    void deleteBeneficiary(Long beneficiaryId, Long userId);
    Beneficiary updateBeneficiary(Long beneficiaryId, Long userId, String nickname, String bank);
}
