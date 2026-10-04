package com.lankatrust.smartbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "atm_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtmCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false, unique = true, length = 30)
    private String cardNumber;

    @Column(nullable = false, length = 255)
    private String cvvHash;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "**** **** **** ****";
        }
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
