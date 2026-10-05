package com.lankatrust.smartbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9\\s\\-\\+\\(\\)]{10,20}$", message = "Please enter a valid phone number")
    private String phoneNumber;

    @NotBlank(message = "NIC is required")
    @Size(min = 10, max = 12, message = "NIC must be between 10 and 12 characters")
    private String nic;

    @NotBlank(message = "Role is required")
    private String role;

    private boolean enabled;
    private boolean googleLinked;
    private LocalDateTime lastLogin;
}

