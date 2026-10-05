package com.lankatrust.smartbank.security;

import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.OtpService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class OtpAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OtpService otpService;

    public OtpAuthenticationSuccessHandler(UserRepository userRepository, OtpService otpService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
        request.getSession().removeAttribute("OTP_VERIFIED");
        try {
            otpService.generateAndSendOtp(authentication.getName(), "LOGIN_STEPUP");
            request.getSession().setAttribute("OTP_SENT", Boolean.TRUE);
        } catch (Exception ex) {
            request.getSession().removeAttribute("OTP_SENT");
        }
        getRedirectStrategy().sendRedirect(request, response, "/login/otp");
    }
}
