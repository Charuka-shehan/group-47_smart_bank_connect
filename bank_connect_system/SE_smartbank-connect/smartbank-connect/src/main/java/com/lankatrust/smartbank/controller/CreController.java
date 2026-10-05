package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.SupportService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cre")
@PreAuthorize("hasAnyRole('CUSTOMER_RELATIONS_EXECUTIVE','BANK_MANAGER')")
public class CreController {

    private final SupportService supportService;
    private final UserService userService;
    private final AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("tickets", supportService.getAllTickets());
        model.addAttribute("statements", supportService.getStatementRequests());
        model.addAttribute("communications", supportService.getCommunications());
        model.addAttribute("customers", userService.searchCustomers(q));
        model.addAttribute("query", q);
        return "cre/dashboard";
    }

    @PostMapping("/tickets")
    public String createTicket(@RequestParam Long customerId,
                               @RequestParam String subject,
                               @RequestParam String message,
                               Authentication authentication,
                               RedirectAttributes ra) {
        try {
            supportService.createTicket(customerId, subject, message, authentication.getName());
            ra.addFlashAttribute("success", "Support ticket created.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cre/dashboard";
    }

    @PostMapping("/tickets/{id}/status")
    public String updateTicket(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String resolution,
                               Authentication authentication,
                               RedirectAttributes ra) {
        supportService.updateStatus(id, status, resolution, authentication.getName());
        ra.addFlashAttribute("success", "Ticket updated.");
        return "redirect:/cre/dashboard";
    }

    @PostMapping("/communications")
    public String logCommunication(@RequestParam Long customerId,
                                   @RequestParam String channel,
                                   @RequestParam String summary,
                                   Authentication authentication,
                                   RedirectAttributes ra) {
        supportService.logCommunication(customerId, authentication.getName(), channel, summary);
        ra.addFlashAttribute("success", "Communication logged.");
        return "redirect:/cre/dashboard";
    }

    @PostMapping("/statements")
    public String requestStatement(@RequestParam Long customerId,
                                   @RequestParam Long accountId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
                                   Authentication authentication,
                                   RedirectAttributes ra) {
        try {
            supportService.requestStatement(customerId, accountId, periodFrom, periodTo, authentication.getName());
            ra.addFlashAttribute("success", "Statement request logged.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cre/dashboard";
    }

    @PostMapping("/statements/{id}/complete")
    public String completeStatement(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        supportService.completeStatement(id, authentication.getName());
        ra.addFlashAttribute("success", "Statement marked complete. Customer notified.");
        return "redirect:/cre/dashboard";
    }

    @GetMapping("/customers/{id}/accounts")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<java.util.Map<String, Object>> accounts(@PathVariable Long id) {
        return accountService.getForCustomer(id).stream()
                .map(a -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", a.getId());
                    map.put("accountNumber", a.getAccountNumber());
                    return map;
                })
                .toList();
    }
}
