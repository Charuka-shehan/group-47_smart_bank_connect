package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.AtmCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtmCardRepository extends JpaRepository<AtmCard, Long> {
    Optional<AtmCard> findByAccountId(Long accountId);
    Optional<AtmCard> findByCardNumber(String cardNumber);
}
