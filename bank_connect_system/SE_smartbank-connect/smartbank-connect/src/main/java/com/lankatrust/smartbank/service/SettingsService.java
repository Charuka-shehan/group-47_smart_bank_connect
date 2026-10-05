package com.lankatrust.smartbank.service;

import java.util.Map;

public interface SettingsService {
    String get(String key, String defaultValue);
    void set(String key, String value);
    Map<String, String> getAll();
    int otpExpiryMinutes();
    int otpMaxAttempts();
    int otpMaxResend();
    int sessionTimeoutMinutes();
    int maxFailedLogins();
}
