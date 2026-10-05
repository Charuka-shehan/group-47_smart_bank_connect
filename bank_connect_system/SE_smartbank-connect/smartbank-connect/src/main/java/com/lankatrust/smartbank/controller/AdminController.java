package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.repository.AuditLogRepository;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.LoanService;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.SettingsService;
import com.lankatrust.smartbank.service.TransactionService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'BANK_MANAGER')")
public class AdminController {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AccountService accountService;
    private final LoanService loanService;
    private final TransactionService transactionService;
    private final SettingsService settingsService;
    private final SessionRegistry sessionRegistry;
    private final Environment environment;
    private final com.lankatrust.smartbank.service.ApprovalService approvalService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalAccounts", accountService.getAll().size());
        model.addAttribute("totalTransactions", transactionService.getAll().size());
        model.addAttribute("pendingLoans", loanService.getAll().stream()
                .filter(l -> l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.APPROVED
                        && l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.REJECTED)
                .count());
        model.addAttribute("settings", settingsService.getAll());
        Runtime rt = Runtime.getRuntime();
        model.addAttribute("heapUsedMb", (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
        model.addAttribute("heapMaxMb", rt.maxMemory() / (1024 * 1024));
        model.addAttribute("uptimeMinutes", ManagementFactory.getRuntimeMXBean().getUptime() / 60000);
        return "admin/dashboard";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(Model model) {
        model.addAttribute("logs", auditLogRepository.findAll());
        return "admin/audit-logs";
    }

    @PostMapping("/audit-logs/{id}/request-delete")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'BANK_MANAGER')")
    public String requestAuditLogDeletion(@PathVariable Long id,
                                          @RequestParam(required = false) String reason,
                                          Authentication authentication,
                                          RedirectAttributes ra) {
        try {
            var log = auditLogRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Audit log entry not found"));
            approvalService.create("AUDIT_LOG_DELETION", id, "Log #" + id + " (" + log.getAction() + ")",
                    authentication.getName(), reason != null ? reason : "Deletion requested by admin");
            ra.addFlashAttribute("success", "Audit log deletion request submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/audit-logs";
    }

    /** Officers can also reach this page (read-only + request forms) to submit staff creation/role/deletion requests. */
    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String staff(Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("users", userService.findAll());
        return "admin/staff";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        var user = userService.getById(id);
        userService.setEnabled(id, !user.isEnabled(), authentication.getName());
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/staff";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("settings", settingsService.getAll());
        model.addAttribute("mailHost", environment.getProperty("spring.mail.host"));
        model.addAttribute("mailUser", environment.getProperty("spring.mail.username"));
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam String otpExpiryMinutes,
                               @RequestParam String otpMaxAttempts,
                               @RequestParam String otpMaxResend,
                               @RequestParam String sessionTimeoutMinutes,
                               @RequestParam String maxFailedLogins,
                               @RequestParam String mailFromName,
                               RedirectAttributes ra) {
        settingsService.set("otp.expiryMinutes", otpExpiryMinutes);
        settingsService.set("otp.maxAttempts", otpMaxAttempts);
        settingsService.set("otp.maxResend", otpMaxResend);
        settingsService.set("session.timeoutMinutes", sessionTimeoutMinutes);
        settingsService.set("security.maxFailedLogins", maxFailedLogins);
        settingsService.set("mail.fromName", mailFromName);
        ra.addFlashAttribute("success", "Settings saved.");
        return "redirect:/admin/settings";
    }

    @GetMapping("/sessions")
    public String sessions(Model model) {
        List<SessionInformation> allSessions = new java.util.ArrayList<>();
        for (Object p : sessionRegistry.getAllPrincipals()) {
            allSessions.addAll(sessionRegistry.getAllSessions(p, false));
        }
        model.addAttribute("principals", sessionRegistry.getAllPrincipals());
        model.addAttribute("sessions", allSessions);
        model.addAttribute("timeoutMinutes", settingsService.sessionTimeoutMinutes());
        return "admin/sessions";
    }

    @PostMapping("/sessions/expire")
    public String expireSession(@RequestParam String sessionId, RedirectAttributes ra) {
        sessionRegistry.getAllPrincipals().forEach(p ->
                sessionRegistry.getAllSessions(p, false).stream()
                        .filter(s -> s.getSessionId().equals(sessionId))
                        .forEach(SessionInformation::expireNow));
        ra.addFlashAttribute("success", "Session expired.");
        return "redirect:/admin/sessions";
    }

    @GetMapping("/monitoring")
    public String monitoring(Model model) {
        Runtime rt = Runtime.getRuntime();
        model.addAttribute("heapUsedMb", (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
        model.addAttribute("heapMaxMb", rt.maxMemory() / (1024 * 1024));
        model.addAttribute("processors", rt.availableProcessors());
        model.addAttribute("uptimeMinutes", ManagementFactory.getRuntimeMXBean().getUptime() / 60000);
        model.addAttribute("users", userService.findAll().size());
        model.addAttribute("accounts", accountService.getAll().size());
        model.addAttribute("transactions", transactionService.getAll().size());
        model.addAttribute("now", LocalDateTime.now());
        return "admin/monitoring";
    }

    @GetMapping("/backup")
    public org.springframework.http.ResponseEntity<byte[]> backup() {
        StringBuilder csv = new StringBuilder("Type,Id,Reference,Details\n");
        userService.findAll().forEach(u -> csv.append("USER,").append(u.getId()).append(",")
                .append(u.getEmail()).append(",").append(u.getRole()).append("\n"));
        accountService.getAll().forEach(a -> csv.append("ACCOUNT,").append(a.getId()).append(",")
                .append(a.getAccountNumber()).append(",").append(a.getStatus()).append("\n"));
        transactionService.getRecentTransactions(200).forEach(t -> csv.append("TXN,").append(t.getId()).append(",")
                .append(t.getReferenceNumber()).append(",").append(t.getAmount()).append("\n"));
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=smartbank-backup.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/view-user-dashboard")
    public String viewUserDashboard() {
        return "redirect:/bankdash/overview";
    }
}
