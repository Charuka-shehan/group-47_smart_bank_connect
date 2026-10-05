package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.Notification;
import com.lankatrust.smartbank.entity.NotificationChannel;
import com.lankatrust.smartbank.entity.User;

import java.util.List;

public interface NotificationService {
    Notification send(User recipient, String title, String message, NotificationChannel channel);
    Notification sendHtml(User recipient, String title, String htmlBody, String plainFallback);
    List<Notification> getForUser(Long userId);
    long unreadCount(Long userId);
    void markAsRead(Long notificationId);
    void markAsRead(Long notificationId, Long userId);
    void deleteNotification(Long notificationId, Long userId);
    com.lankatrust.smartbank.entity.NotificationPreference getPreferences(Long userId);
    com.lankatrust.smartbank.entity.NotificationPreference updatePreferences(Long userId, boolean email, boolean sms, boolean inApp);
}
