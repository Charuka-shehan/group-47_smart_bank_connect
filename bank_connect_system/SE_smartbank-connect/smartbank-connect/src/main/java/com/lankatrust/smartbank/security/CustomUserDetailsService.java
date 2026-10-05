package com.lankatrust.smartbank.security;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .or(() -> username.contains("@")
                        ? java.util.Optional.empty()
                        : userRepository.findByEmail(username + "@lankatrust.lk"))
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + username));

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new UsernameNotFoundException("Account is temporarily locked. Please try again later.");
        }

        // Check if account is disabled
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("Account is disabled. Please contact support.");
        }

        return new CustomUserDetails(user);
    }
}
