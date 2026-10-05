package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByUserId(Long userId);
    List<Beneficiary> findByBeneficiaryAccountNumber(String beneficiaryAccountNumber);
    void deleteByBeneficiaryAccountNumber(String beneficiaryAccountNumber);
}
