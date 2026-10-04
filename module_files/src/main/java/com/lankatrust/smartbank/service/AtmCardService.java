package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.AtmCard;

import java.util.Optional;

public interface AtmCardService {
    Optional<AtmCard> findByAccountId(Long accountId);
    Optional<AtmCard> findByCardNumber(String cardNumber);
    AtmCard blockCard(Long cardId);
    AtmCard unblockCard(Long cardId);
}
