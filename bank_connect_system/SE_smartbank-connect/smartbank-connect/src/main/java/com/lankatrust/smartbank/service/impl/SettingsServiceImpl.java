package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.AppMeta;
import com.lankatrust.smartbank.repository.AppMetaRepository;
import com.lankatrust.smartbank.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final AppMetaRepository appMetaRepository;

    @Override
    public String get(String key, String defaultValue) {
        return appMetaRepository.findById(key).map(AppMeta::getMetaValue).orElse(defaultValue);
    }

    @Override
    @Transactional
    public void set(String key, String value) {
        AppMeta meta = appMetaRepository.findById(key).orElse(AppMeta.builder().metaKey(key).build());
        meta.setMetaValue(value);
        meta.setUpdatedAt(LocalDateTime.now());
        appMetaRepository.save(meta);
    }

    @Override
    public Map<String, String> getAll() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("otp.expiryMinutes", get("otp.expiryMinutes", "5"));
        map.put("otp.maxAttempts", get("otp.maxAttempts", "5"));
        map.put("otp.maxResend", get("otp.maxResend", "3"));
        map.put("session.timeoutMinutes", get("session.timeoutMinutes", "15"));
        map.put("security.maxFailedLogins", get("security.maxFailedLogins", "5"));
        map.put("mail.fromName", get("mail.fromName", "SmartBank Connect"));
        map.put("password.minLength", get("password.minLength", "8"));
        return map;
    }

    @Override
    public int otpExpiryMinutes() {
        return parseInt("otp.expiryMinutes", 5);
    }

    @Override
    public int otpMaxAttempts() {
        return parseInt("otp.maxAttempts", 5);
    }

    @Override
    public int otpMaxResend() {
        return parseInt("otp.maxResend", 3);
    }

    @Override
    public int sessionTimeoutMinutes() {
        return parseInt("session.timeoutMinutes", 15);
    }

    @Override
    public int maxFailedLogins() {
        return parseInt("security.maxFailedLogins", 5);
    }

    private int parseInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, String.valueOf(fallback)));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
