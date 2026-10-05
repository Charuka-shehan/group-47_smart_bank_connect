package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.ApprovalService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping({"/approvals", "/manager/approvals"})
@PreAuthorize("hasRole('BANK_MANAGER')")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final AccountService accountService;
    private final UserService userService;

    private String resolveEmail(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth) {
            String email = oauth.getAttribute("email");
            if (email != null && !email.isBlank()) return email.trim();
        }
        return authentication.getName() != null ? authentication.getName().trim() : null;
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
        if (authentication != null) {
            try {
                String email = resolveEmail(authentication);
                if (email != null) {
                    model.addAttribute("user", userService.getByEmail(email));
                }
            } catch (Exception ignored) {}
        }
        model.addAttribute("pending", approvalService.getPending());
        model.addAttribute("all", approvalService.getAll());
        model.addAttribute("pendingAccounts", accountService.getAll().stream()
                .filter(a -> a.getStatus() == com.lankatrust.smartbank.entity.AccountStatus.PENDING_APPROVAL)
                .toList());
        return "approvals/list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(required = false) String remarks,
                          Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String email = resolveEmail(authentication);
            approvalService.approve(id, email, remarks);
            redirectAttributes.addFlashAttribute("success", "Request approved and executed successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/approvals";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String remarks,
                         Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String email = resolveEmail(authentication);
            approvalService.reject(id, email, remarks);
            redirectAttributes.addFlashAttribute("success", "Request rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/approvals";
    }
}
