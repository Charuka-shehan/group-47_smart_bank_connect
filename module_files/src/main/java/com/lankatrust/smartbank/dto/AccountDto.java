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
public class AccountDto {
    private Long id;
    
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[A-Z0-9]{3,20}$", message = "Invalid account number format")
    private String accountNumber;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(SAVINGS|CURRENT|FIXED_DEPOSIT)$", message = "Invalid account type")
    private String accountType;
    
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Balance cannot be negative")
    private BigDecimal balance;
    
    @NotBlank(message = "Account status is required")
    private String status;
    
    private LocalDateTime openedAt;
}

