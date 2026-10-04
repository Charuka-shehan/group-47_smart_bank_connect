package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.AtmCard;
import com.lankatrust.smartbank.repository.AtmCardRepository;
import com.lankatrust.smartbank.service.AtmCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AtmCardServiceImpl implements AtmCardService {

    private final AtmCardRepository atmCardRepository;

    @Override
    public Optional<AtmCard> findByAccountId(Long accountId) {
        return atmCardRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<AtmCard> findByCardNumber(String cardNumber) {
        return atmCardRepository.findByCardNumber(cardNumber);
    }

    @Override
    public AtmCard blockCard(Long cardId) {
        AtmCard card = atmCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        card.setStatus("BLOCKED");
        return atmCardRepository.save(card);
    }

    @Override
    public AtmCard unblockCard(Long cardId) {
        AtmCard card = atmCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        card.setStatus("ACTIVE");
        return atmCardRepository.save(card);
    }
}
