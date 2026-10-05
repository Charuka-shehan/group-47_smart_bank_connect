package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.LoanService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;

    /** Customer: C - submit application, R - view own applications & status. */
    @GetMapping
    public String myLoans(Authentication authentication, Model model) {
        User customer = userService.getByEmail(authentication.getName());
        model.addAttribute("loans", loanService.getForCustomer(customer.getId()));
        return "loans/my-loans";
    }

    @GetMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String applyForm() {
        return "loans/apply";
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String apply(@RequestParam String loanType, @RequestParam BigDecimal amount,
                         @RequestParam BigDecimal monthlyIncome, @RequestParam String employmentStatus,
                         @RequestParam String documentsSummary, Authentication authentication,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            loanService.submitApplication(customer.getId(), loanType, amount, monthlyIncome,
                    employmentStatus, documentsSummary);
            redirectAttributes.addFlashAttribute("success", "Loan application submitted successfully!");
            return "redirect:/loans";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/loans/apply";
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String cancelApplication(@PathVariable Long id, Authentication authentication,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            loanService.cancel(id, customer.getId());
            redirectAttributes.addFlashAttribute("success", "Loan application cancelled successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/{id}/update")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String updateApplication(@PathVariable Long id,
                                    @RequestParam String loanType,
                                    @RequestParam BigDecimal amount,
                                    @RequestParam BigDecimal monthlyIncome,
                                    @RequestParam String employmentStatus,
                                    @RequestParam(required = false) String documentsSummary,
                                    Authentication authentication,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User customer = userService.getByEmail(authentication.getName());
            loanService.updateApplication(id, customer.getId(), loanType, amount, monthlyIncome, employmentStatus, documentsSummary);
            redirectAttributes.addFlashAttribute("success", "Loan application updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/loans";
    }

    /** Officer/Compliance/Manager review queue. */
    @GetMapping("/review")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','COMPLIANCE_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String reviewQueue(Model model) {
        model.addAttribute("loans", loanService.getAll());
        return "loans/review";
    }

    /** U: Officer verifies documents/completeness. */
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','SYSTEM_ADMINISTRATOR')")
    public String verify(@PathVariable Long id, @RequestParam(required = false) String remarks,
                          Authentication authentication) {
        loanService.verify(id, authentication.getName(), remarks);
        return "redirect:/loans/review";
    }

    /** Compliance Officer: regulatory / risk check. */
    @PostMapping("/{id}/compliance")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','BANK_MANAGER')")
    public String complianceCheck(@PathVariable Long id, @RequestParam(required = false) String remarks,
                                   Authentication authentication) {
        loanService.complianceCheck(id, authentication.getName(), remarks);
        return "redirect:/loans/review";
    }

    /** Manager: final approval. */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String approve(@PathVariable Long id, Authentication authentication) {
        loanService.approve(id, authentication.getName());
        return "redirect:/loans/review";
    }

    /** D (functionally: reject) - Officer/Manager reject invalid applications. */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String reject(@PathVariable Long id, @RequestParam String reason, Authentication authentication) {
        loanService.reject(id, authentication.getName(), reason);
        return "redirect:/loans/review";
    }
}
