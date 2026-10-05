package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@lombok.extern.slf4j.Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OtpApiController {

    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, Object> response = new HashMap<>();
        try {
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid email address");
                return ResponseEntity.badRequest().body(response);
            }
            otpService.generateAndSendOtp(email, "API_REQUEST");
            response.put("success", true);
            response.put("message", "OTP sent successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("API send-otp error: ", e);
            response.put("success", false);
            response.put("message", "Unable to send verification code. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        Map<String, Object> response = new HashMap<>();
        try {
            if (email == null || otp == null) {
                response.put("success", false);
                response.put("message", "Email and OTP are required");
                return ResponseEntity.badRequest().body(response);
            }
            boolean verified = otpService.verifyOtp(email, otp);
            if (verified) {
                response.put("success", true);
                response.put("message", "Email verified successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Incorrect OTP");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("API verify-otp error: ", e);
            response.put("success", false);
            response.put("message", "Unable to verify code at this time. Please try again.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, Object> response = new HashMap<>();
        try {
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid email address");
                return ResponseEntity.badRequest().body(response);
            }
            otpService.generateAndSendOtp(email, "API_REQUEST");
            response.put("success", true);
            response.put("message", "OTP resent successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("API resend-otp error: ", e);
            response.put("success", false);
            response.put("message", "Unable to resend verification code. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
