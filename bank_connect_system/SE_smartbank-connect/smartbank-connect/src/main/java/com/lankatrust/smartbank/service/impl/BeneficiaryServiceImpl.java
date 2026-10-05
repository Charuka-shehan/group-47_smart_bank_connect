package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.Beneficiary;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.BeneficiaryRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Beneficiary addBeneficiary(Long userId, String beneficiaryName, String accountNumber, String bank, String nickname) {
        if (beneficiaryName == null || beneficiaryName.trim().length() < 2 || beneficiaryName.trim().length() > 100) {
            throw new IllegalArgumentException("Beneficiary name must be between 2 and 100 characters.");
        }
        if (accountNumber == null || accountNumber.trim().length() < 6 || accountNumber.trim().length() > 20) {
            throw new IllegalArgumentException("Beneficiary account number must be between 6 and 20 characters.");
        }
        String cleanAccount = accountNumber.trim();
        String cleanBank = (bank != null && !bank.isBlank()) ? bank.trim() : "LankaTrust Bank";

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check duplicate beneficiary for this user
        boolean exists = beneficiaryRepository.findByUserId(userId).stream()
                .anyMatch(b -> b.getBeneficiaryAccountNumber().equalsIgnoreCase(cleanAccount) &&
                               b.getBeneficiaryBank().equalsIgnoreCase(cleanBank));
        if (exists) {
            throw new IllegalArgumentException("A beneficiary with this account number and bank already exists.");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .user(user)
                .beneficiaryName(beneficiaryName.trim())
                .beneficiaryAccountNumber(cleanAccount)
                .beneficiaryBank(cleanBank)
                .nickname(nickname != null && !nickname.isBlank() ? nickname.trim() : null)
                .build();
        return beneficiaryRepository.save(beneficiary);
    }

    @Override
    public List<Beneficiary> getForUser(Long userId) {
        return beneficiaryRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long beneficiaryId) {
        beneficiaryRepository.deleteById(beneficiaryId);
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long beneficiaryId, Long userId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found."));
        if (!beneficiary.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to delete this beneficiary.");
        }
        beneficiaryRepository.delete(beneficiary);
    }

    @Override
    @Transactional
    public Beneficiary updateBeneficiary(Long beneficiaryId, Long userId, String nickname, String bank) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found."));
        if (!beneficiary.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to update this beneficiary.");
        }
        if (nickname != null) {
            beneficiary.setNickname(nickname.trim());
        }
        if (bank != null && !bank.isBlank()) {
            beneficiary.setBeneficiaryBank(bank.trim());
        }
        return beneficiaryRepository.save(beneficiary);
    }
}
