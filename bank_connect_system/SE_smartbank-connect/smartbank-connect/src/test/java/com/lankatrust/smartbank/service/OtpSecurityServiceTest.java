package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.NotificationChannel;
import com.lankatrust.smartbank.entity.OtpVerification;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.OtpVerificationRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.impl.OtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpSecurityServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpVerificationRepository otpVerificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private SettingsService settingsService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpServiceImpl otpService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpThreshold", new BigDecimal("50000.00"));
        lenient().when(settingsService.otpMaxResend()).thenReturn(3);
        lenient().when(settingsService.otpExpiryMinutes()).thenReturn(5);
        testUser = User.builder()
                .id(1L)
                .email("customer@lankatrust.lk")
                .fullName("Test Customer")
                .phoneNumber("0771234567")
                .build();
    }

    @Test
    void isOtpRequiredReturnsTrueForMandatorySensitiveOperations() {
        assertTrue(otpService.isOtpRequired("CARD_PAYMENT", BigDecimal.TEN, false));
        assertTrue(otpService.isOtpRequired("BENEFICIARY_ADD", BigDecimal.ZERO, false));
        assertTrue(otpService.isOtpRequired("PASSWORD_CHANGE", BigDecimal.ZERO, false));
        assertTrue(otpService.isOtpRequired("LOAN_APPLICATION", new BigDecimal("100000"), false));
        assertTrue(otpService.isOtpRequired("EMAIL_CHANGE", BigDecimal.ZERO, false));
    }

    @Test
    void isOtpRequiredReturnsTrueForHighValueOrExternalTransfer() {
        // High-value transfer
        assertTrue(otpService.isOtpRequired("FUND_TRANSFER", new BigDecimal("75000.00"), false));
        // New or external beneficiary
        assertTrue(otpService.isOtpRequired("FUND_TRANSFER", new BigDecimal("5000.00"), true));
        // Low-value existing transfer does not require OTP
        assertFalse(otpService.isOtpRequired("FUND_TRANSFER", new BigDecimal("5000.00"), false));
    }

    @Test
    void generateAndSendOtpHashesCodeAndSendsExclusivelyViaEmail() {
        when(userRepository.findByEmail("customer@lankatrust.lk")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedOtpCode1234567890");
        when(otpVerificationRepository.save(any(OtpVerification.class))).thenAnswer(i -> i.getArgument(0));

        otpService.generateAndSendOtp("customer@lankatrust.lk", "FUND_TRANSFER");

        // Verify OTP is hashed before saving to repository
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(captor.capture());
        OtpVerification saved = captor.getValue();

        assertNotNull(saved.getOtpHash());
        assertEquals("$2a$10$hashedOtpCode1234567890", saved.getOtpHash());
        assertEquals("FUND_TRANSFER", saved.getOperationType());
        assertFalse(saved.isVerified());

        // Verify notification is dispatched to email ONLY, never phone/SMS
        verify(notificationService).sendHtml(eq(testUser), anyString(), anyString(), anyString());
        verify(notificationService, never()).send(any(), anyString(), anyString(), eq(NotificationChannel.SMS));
    }

    @Test
    void verifyOtpSucceedsWithCorrectCode() {
        when(settingsService.otpMaxAttempts()).thenReturn(3);

        OtpVerification entity = OtpVerification.builder()
                .id(100L)
                .email("customer@lankatrust.lk")
                .operationType("CARD_PAYMENT")
                .otpHash("$2a$10$hashedOtpCode")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .build();

        when(otpVerificationRepository.findFirstByEmailOrderByCreatedAtDesc("customer@lankatrust.lk"))
                .thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("123456", "$2a$10$hashedOtpCode")).thenReturn(true);
        when(otpVerificationRepository.save(any(OtpVerification.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = otpService.verifyOtp("customer@lankatrust.lk", "123456");

        assertTrue(result);
        assertTrue(entity.isVerified());
    }

    @Test
    void verifyOtpFailsWithIncorrectCodeAndIncrementsAttempts() {
        when(settingsService.otpMaxAttempts()).thenReturn(3);

        OtpVerification entity = OtpVerification.builder()
                .id(100L)
                .email("customer@lankatrust.lk")
                .operationType("CARD_PAYMENT")
                .otpHash("$2a$10$hashedOtpCode")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(1)
                .verified(false)
                .build();

        when(otpVerificationRepository.findFirstByEmailOrderByCreatedAtDesc("customer@lankatrust.lk"))
                .thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("999999", "$2a$10$hashedOtpCode")).thenReturn(false);

        boolean result = otpService.verifyOtp("customer@lankatrust.lk", "999999");

        assertFalse(result);
        assertEquals(2, entity.getAttempts());
        assertFalse(entity.isVerified());
    }

    @Test
    void verifyOtpRejectsExpiredCode() {
        OtpVerification entity = OtpVerification.builder()
                .id(100L)
                .email("customer@lankatrust.lk")
                .operationType("CARD_PAYMENT")
                .otpHash("$2a$10$hashedOtpCode")
                .expiresAt(LocalDateTime.now().minusMinutes(1)) // expired
                .attempts(0)
                .verified(false)
                .build();

        when(otpVerificationRepository.findFirstByEmailOrderByCreatedAtDesc("customer@lankatrust.lk"))
                .thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () ->
                otpService.verifyOtp("customer@lankatrust.lk", "123456"));
    }
}
