package com.lankatrust.smartbank.security;

import com.lankatrust.smartbank.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class OtpInterceptor implements HandlerInterceptor {

    private final OtpService otpService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String path = request.getServletPath();
            if (path == null || path.isBlank()) {
                path = request.getRequestURI();
            }

            // Allow static assets, uploads, error pages, API, Swagger, and OTP endpoints
            if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                    || path.startsWith("/webjars/") || path.startsWith("/uploads/") || path.equals("/error")
                    || path.startsWith("/api/") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                    || path.equals("/login/otp") || path.equals("/login/otp/verify") || path.equals("/login/otp/resend")
                    || path.equals("/logout")) {
                return true;
            }

            HttpSession session = request.getSession();
            Boolean otpVerified = (Boolean) session.getAttribute("OTP_VERIFIED");

            if (otpVerified == null || !otpVerified) {
                // If OTP has not been sent yet in this session, generate and send it
                if (session.getAttribute("OTP_SENT") == null) {
                    try {
                        otpService.generateAndSendOtp(auth.getName(), "LOGIN_STEPUP");
                        session.setAttribute("OTP_SENT", Boolean.TRUE);
                    } catch (Exception e) {
                        // User not found or not seeded yet
                    }
                }
                response.sendRedirect(request.getContextPath() + "/login/otp");
                return false;
            }
        }
        return true;
    }
}
