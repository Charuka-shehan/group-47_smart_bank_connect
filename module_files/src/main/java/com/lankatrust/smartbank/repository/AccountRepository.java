package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(Long customerId);
    List<Account> findByStatus(AccountStatus status);
}
