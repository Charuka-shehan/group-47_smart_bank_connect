package com.lankatrust.smartbank.config;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationStartup implements ApplicationRunner {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check app_meta flag
        String printed = null;
        try {
            printed = jdbcTemplate.queryForObject("SELECT meta_value FROM app_meta WHERE meta_key = ?", new Object[]{"manager_credentials_printed"}, String.class);
        } catch (Exception ex) {
            // table or row may not exist; ignore
        }

        if (!"true".equalsIgnoreCase(printed)) {
            // Ensure manager user exists
            User manager = userRepository.findByEmail("manager@lankatrust.lk").orElse(null);
            if (manager != null) {
                // Set a known password for first start
                String plain = "Manager@123";
                manager.setPassword(passwordEncoder.encode(plain));
                userRepository.save(manager);

                // Print credentials to console once
                System.out.println("\n=== Initial Bank Manager Credentials ===");
                System.out.println("Email: manager@lankatrust.lk");
                System.out.println("Password: " + plain);
                System.out.println("=======================================\n");

                // Mark flag
                try {
                    jdbcTemplate.update("INSERT INTO app_meta(meta_key, meta_value) VALUES(?, ?) ON DUPLICATE KEY UPDATE meta_value = ?", "manager_credentials_printed", "true", "true");
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
    }
}

