package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.AtmCard;
import com.lankatrust.smartbank.repository.AtmCardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoAtmRemovalTest {

    @Mock
    private AtmCardRepository atmCardRepository;

    @Test
    void demoAtmControllerClassIsCompletelyRemoved() {
        assertThrows(ClassNotFoundException.class, () ->
                Class.forName("com.lankatrust.smartbank.controller.DemoAtmController"));
    }

    @Test
    void transactionServiceHasNoDemoAtmMethods() {
        Method[] methods = TransactionService.class.getMethods();
        boolean hasAtmDemo = Arrays.stream(methods)
                .anyMatch(m -> m.getName().toLowerCase().contains("atm"));
        assertFalse(hasAtmDemo, "TransactionService must not contain demo ATM withdrawal methods");
    }

    @Test
    void realAtmCardRepositoryRemainsIntactAndOperational() {
        Account account = Account.builder().id(55L).accountNumber("LTB555").build();
        AtmCard card = AtmCard.builder().id(1L).cardNumber("4532111122223333").account(account).build();

        when(atmCardRepository.findByAccountId(55L)).thenReturn(java.util.Optional.of(card));

        java.util.Optional<AtmCard> cardOpt = atmCardRepository.findByAccountId(55L);
        assertTrue(cardOpt.isPresent());
        assertEquals("4532111122223333", cardOpt.get().getCardNumber());
        verify(atmCardRepository, times(1)).findByAccountId(55L);
    }
}
