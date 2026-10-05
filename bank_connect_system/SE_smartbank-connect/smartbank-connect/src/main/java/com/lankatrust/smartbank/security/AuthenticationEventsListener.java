package com.lankatrust.smartbank.security;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationEventsListener {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            auditLogService.log(email, "LOGIN_SUCCESS", "User", user.getId(), "User logged in successfully");
        }
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                auditLogService.log(email, "ACCOUNT_LOCKED", "User", user.getId(), "Account locked for 15 minutes due to 5 failed attempts");
            } else {
                auditLogService.log(email, "LOGIN_FAILED", "User", user.getId(), "Failed login attempt #" + attempts);
            }
            userRepository.save(user);
        }
    }
}
