package com.lankatrust.smartbank.security;

import com.lankatrust.smartbank.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Value("${security.login.maxFailedAttempts:5}")
    private int maxFailedAttempts;

    @Value("${security.login.lockDurationMinutes:30}")
    private int lockDurationMinutes;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");

        // Try to find user by email (with or without @lankatrust.lk domain)
        var userOpt = userRepository.findByEmail(username)
                .or(() -> username != null && !username.contains("@")
                        ? userRepository.findByEmail(username + "@lankatrust.lk")
                        : java.util.Optional.empty());

        final String[] failureUrl = {"/login?error=true"};

        userOpt.ifPresent(user -> {
            // Check if account is locked
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                failureUrl[0] = "/login?locked=true";
                return;
            }

            // Check if account is disabled
            if (!user.isEnabled()) {
                failureUrl[0] = "/login?disabled=true";
                return;
            }

            // Increment failed attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // Lock account if max attempts reached
            if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            }

            userRepository.save(user);
        });

        super.setDefaultFailureUrl(failureUrl[0]);
        super.onAuthenticationFailure(request, response, exception);
    }
}

