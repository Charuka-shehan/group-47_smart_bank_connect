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
public class LoanApplicationDto {
    private Long id;
    
    @NotNull(message = "Applicant ID is required")
    private Long applicantId;
    
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000.00", inclusive = true, message = "Minimum loan amount is Rs. 10,000")
    @DecimalMax(value = "50000000.00", inclusive = true, message = "Maximum loan amount is Rs. 50,000,000")
    private BigDecimal amount;
    
    @NotBlank(message = "Loan purpose is required")
    @Size(min = 10, max = 500, message = "Loan purpose must be between 10 and 500 characters")
    private String purpose;
    
    @NotNull(message = "Loan term (in months) is required")
    @Min(value = 3, message = "Minimum loan term is 3 months")
    @Max(value = 360, message = "Maximum loan term is 30 years (360 months)")
    private Integer termMonths;
    
    private String status;
    private LocalDateTime submittedAt;
}

