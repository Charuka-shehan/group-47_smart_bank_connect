package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/notifications")
    public String list(Authentication authentication, Model model) {
        User user = userService.getByEmail(authentication.getName());
        model.addAttribute("notifications", notificationService.getForUser(user.getId()));
        model.addAttribute("preferences", notificationService.getPreferences(user.getId()));
        return "notifications/list";
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(@PathVariable Long id, Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        notificationService.markAsRead(id, user.getId());
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            User user = userService.getByEmail(authentication.getName());
            notificationService.deleteNotification(id, user.getId());
            ra.addFlashAttribute("success", "Notification removed.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/preferences")
    public String updatePreferences(@RequestParam(defaultValue = "false") boolean emailNotifications,
                                    @RequestParam(defaultValue = "false") boolean smsNotifications,
                                    @RequestParam(defaultValue = "false") boolean inAppNotifications,
                                    Authentication authentication,
                                    RedirectAttributes ra) {
        try {
            User user = userService.getByEmail(authentication.getName());
            notificationService.updatePreferences(user.getId(), emailNotifications, smsNotifications, inAppNotifications);
            ra.addFlashAttribute("success", "Notification preferences saved successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/notifications";
    }
}
