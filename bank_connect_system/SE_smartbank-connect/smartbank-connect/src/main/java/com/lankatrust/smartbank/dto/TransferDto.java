package com.lankatrust.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDto {
    private Long id;
    private String referenceNumber;
    
    @NotNull(message = "Source account is required")
    private Long fromAccountId;
    
    private String toAccountNumber;
    private String toAccountBank;
    private Long toUserId;
    
    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least Rs. 0.01")
    @DecimalMax(value = "999999999.99", inclusive = true, message = "Amount exceeds maximum limit")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private String status;
    private Long requestedBy;
    private LocalDateTime requestedAt;
}

