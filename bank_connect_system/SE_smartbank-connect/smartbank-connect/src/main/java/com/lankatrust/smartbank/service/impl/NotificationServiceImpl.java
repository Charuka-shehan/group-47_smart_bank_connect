package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.Notification;
import com.lankatrust.smartbank.entity.NotificationChannel;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.entity.NotificationPreference;
import com.lankatrust.smartbank.repository.NotificationPreferenceRepository;
import com.lankatrust.smartbank.repository.NotificationRepository;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Member 6 (Weerasingha K. A. H. U.) - Notifications.
 * In this academic prototype, SMS/Email dispatch is simulated by logging
 * (see Section 9 - Budget: "sandbox or simulated services"). The in-app
 * notification row is always persisted so the customer sees a live feed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;
    private final org.springframework.mail.javamail.JavaMailSender mailSender;

    @Override
    public Notification send(User recipient, String title, String message, NotificationChannel channel) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .channel(channel)
                .build();
        Notification saved = notificationRepository.save(notification);

        if (channel == NotificationChannel.SMS) {
            log.info("[SIMULATED SMS] to {} -> {}", recipient.getPhoneNumber(), message);
        } else if (channel == NotificationChannel.EMAIL) {
            try {
                org.springframework.mail.SimpleMailMessage mailMessage = new org.springframework.mail.SimpleMailMessage();
                mailMessage.setTo(recipient.getEmail());
                mailMessage.setSubject(title);
                mailMessage.setText(message);
                mailSender.send(mailMessage);
                log.info("Email sent successfully to {}", recipient.getEmail());
            } catch (Exception e) {
                log.error("Failed to send email to {}", recipient.getEmail(), e);
            }
        }
        return saved;
    }

    @Override
    public Notification sendHtml(User recipient, String title, String htmlBody, String plainFallback) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(plainFallback != null ? plainFallback : title)
                .channel(NotificationChannel.EMAIL)
                .build();
        Notification saved = notificationRepository.save(notification);
        try {
            jakarta.mail.internet.MimeMessage mime = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper =
                    new org.springframework.mail.javamail.MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(recipient.getEmail());
            helper.setSubject(title);
            helper.setText(plainFallback != null ? plainFallback : title, htmlBody);
            mailSender.send(mime);
            log.info("HTML email sent successfully to {}", recipient.getEmail());
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}", recipient.getEmail(), e);
        }
        return saved;
    }

    @Override
    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient() != null && n.getRecipient().getId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient() != null && n.getRecipient().getId().equals(userId)) {
                notificationRepository.delete(n);
            }
        });
    }

    @Override
    @Transactional
    public NotificationPreference getPreferences(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                    NotificationPreference def = NotificationPreference.builder()
                            .user(user)
                            .emailNotifications(true)
                            .smsNotifications(false)
                            .inAppNotifications(true)
                            .build();
                    return notificationPreferenceRepository.save(def);
                });
    }

    @Override
    @Transactional
    public NotificationPreference updatePreferences(Long userId, boolean email, boolean sms, boolean inApp) {
        NotificationPreference pref = getPreferences(userId);
        pref.setEmailNotifications(email);
        pref.setSmsNotifications(sms);
        pref.setInAppNotifications(inApp);
        return notificationPreferenceRepository.save(pref);
    }
}
