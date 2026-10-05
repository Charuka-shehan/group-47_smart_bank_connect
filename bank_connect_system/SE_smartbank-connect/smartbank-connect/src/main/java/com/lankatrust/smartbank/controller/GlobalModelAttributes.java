package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * common/sidebar.jsp (included on every internal page) reads ${user.role}
 * and ${unreadCount} to decide which navigation links and notification
 * badge to render. Rather than repeating that lookup in every controller,
 * this advice populates both attributes once, for every request handled
 * by an @Controller, before the handler method runs.
 */
@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserService userService;
    private final NotificationService notificationService;

    @ModelAttribute
    public void addCurrentUser(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return;
        }
        try {
            User user = userService.getByEmail(authentication.getName());
            model.addAttribute("user", user);
            model.addAttribute("unreadCount", notificationService.unreadCount(user.getId()));
        } catch (IllegalArgumentException ignored) {
            // No matching local user yet (e.g. mid-OAuth2 handshake); pages
            // that need "user" will simply show the generic/guest sidebar.
        }
    }
}
