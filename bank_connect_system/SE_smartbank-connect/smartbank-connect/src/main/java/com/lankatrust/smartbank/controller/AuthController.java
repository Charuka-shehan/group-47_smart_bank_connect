package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@lombok.extern.slf4j.Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final com.lankatrust.smartbank.service.AccountService accountService;

    @GetMapping("/login")
    public String login(org.springframework.security.core.Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/bankdash/overview";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(org.springframework.security.core.Authentication authentication, Model model) {
        if (isAuthenticated(authentication)) {
            return "redirect:/bankdash/overview";
        }
        model.addAttribute("accountTypes", new String[]{"SAVINGS", "CURRENT", "FIXED_DEPOSIT"});
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String customerId,
                           @RequestParam String accountNumber,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {
        try {
            userService.registerCustomer(customerId, accountNumber, email, phone, username, password, confirmPassword);
            model.addAttribute("success", "Registration successful! Please log in with your email and password.");
            return "login";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("accountTypes", new String[]{"SAVINGS", "CURRENT", "FIXED_DEPOSIT"});
            return "register";
        } catch (Exception ex) {
            log.error("Customer registration error: ", ex);
            model.addAttribute("error", "Registration could not be completed. Please verify your details and try again.");
            model.addAttribute("accountTypes", new String[]{"SAVINGS", "CURRENT", "FIXED_DEPOSIT"});
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
            userService.requestPasswordReset(email);
            model.addAttribute("success", "If the email exists, a reset link has been sent. In sandbox mode the link is also printed in the application console.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            log.error("Password reset request error: ", ex);
            model.addAttribute("error", "Unable to process password reset request. Please try again later.");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model model) {
        try {
            userService.resetPassword(token, newPassword, confirmPassword);
            model.addAttribute("success", "Password reset successfully. You can now log in.");
            return "login";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        } catch (Exception ex) {
            log.error("Password reset error: ", ex);
            model.addAttribute("error", "Failed to reset password. Please try again or request a new reset link.");
            model.addAttribute("token", token);
            return "reset-password";
        }
    }

    @GetMapping("/login/otp")
    public String loginOtp(org.springframework.security.core.Authentication authentication, Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }
        return "login-otp";
    }

    @PostMapping("/login/otp/verify")
    public String verifyLoginOtp(@RequestParam String otp, jakarta.servlet.http.HttpSession session,
                                 org.springframework.security.core.Authentication authentication, Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }
        try {
            boolean isValid = userService.verifyOtp(authentication.getName(), otp);
            if (isValid) {
                session.setAttribute("OTP_VERIFIED", Boolean.TRUE);
                User user = userService.getByEmail(authentication.getName());
                return "redirect:" + dashboardFor(user);
            }
            model.addAttribute("error", "Invalid or expired OTP code.");
            return "login-otp";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login-otp";
        } catch (Exception ex) {
            log.error("Error verifying login OTP for user {}", authentication.getName(), ex);
            model.addAttribute("error", "An error occurred while verifying the code. Please try again.");
            return "login-otp";
        }
    }

    @GetMapping("/login/otp/resend")
    public String resendLoginOtp(org.springframework.security.core.Authentication authentication, Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }
        try {
            userService.generateAndSendOtp(authentication.getName());
            model.addAttribute("success", "A new OTP code has been sent to your registered email address.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to resend login OTP for user {}", authentication.getName(), e);
            model.addAttribute("error", "Failed to resend verification code. Please wait a moment and try again.");
        }
        return "login-otp";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    private String dashboardFor(User user) {
        return switch (user.getRole()) {
            case CUSTOMER -> "/customer/dashboard";
            case BANK_OFFICER -> "/officer/dashboard";
            case CUSTOMER_RELATIONS_EXECUTIVE -> "/cre/dashboard";
            case SYSTEM_ADMINISTRATOR -> "/admin/dashboard";
            case COMPLIANCE_OFFICER -> "/compliance/dashboard";
            case BANK_MANAGER -> "/manager/dashboard";
        };
    }

    private boolean isAuthenticated(org.springframework.security.core.Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
    }
}
