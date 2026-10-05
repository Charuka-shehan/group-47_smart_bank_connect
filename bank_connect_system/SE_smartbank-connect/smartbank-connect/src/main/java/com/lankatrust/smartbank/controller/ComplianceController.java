package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.AccountStatus;
import com.lankatrust.smartbank.entity.Customer;
import com.lankatrust.smartbank.entity.CustomerDocument;
import com.lankatrust.smartbank.entity.LoanStatus;
import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.Transaction;
import com.lankatrust.smartbank.entity.TransactionStatus;
import com.lankatrust.smartbank.repository.CustomerDocumentRepository;
import com.lankatrust.smartbank.repository.CustomerRepository;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.LoanService;
import com.lankatrust.smartbank.service.TransactionService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/compliance")
@PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','BANK_MANAGER')")
public class ComplianceController {

    private static final BigDecimal AML_THRESHOLD = new BigDecimal("1000000.00");

    private final UserService userService;
    private final CustomerRepository customerRepository;
    private final CustomerDocumentRepository customerDocumentRepository;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final AccountService accountService;
    private final AuditLogService auditLogService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Customer> customers = customerRepository.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("pendingKyc", customers.stream()
                .filter(c -> c.getKycStatus() == null || !"APPROVED".equalsIgnoreCase(c.getKycStatus()))
                .toList());
        model.addAttribute("documents", customerDocumentRepository.findAll());
        List<Transaction> suspicious = transactionService.getAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED
                        && t.getAmount() != null && t.getAmount().compareTo(AML_THRESHOLD) >= 0)
                .toList();
        model.addAttribute("suspicious", suspicious);
        model.addAttribute("loans", loanService.getAll());
        model.addAttribute("frozenAccounts", accountService.getAll().stream()
                .filter(a -> a.getStatus() == AccountStatus.FROZEN).toList());
        return "compliance/dashboard";
    }

    @PostMapping("/kyc/{id}/verify")
    public String verifyKyc(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        customer.setKycStatus("APPROVED");
        customerRepository.save(customer);
        auditLogService.log(authentication.getName(), "KYC_VERIFIED", "Customer", id, customer.getCustomerId());
        ra.addFlashAttribute("success", "KYC verified for " + customer.getFullName());
        return "redirect:/compliance/dashboard";
    }

    @PostMapping("/kyc/{id}/flag")
    public String flagKyc(@PathVariable Long id, @RequestParam String reason, Authentication authentication, RedirectAttributes ra) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        customer.setKycStatus("HIGH_RISK");
        customerRepository.save(customer);
        auditLogService.log(authentication.getName(), "KYC_FLAGGED", "Customer", id, reason);
        ra.addFlashAttribute("success", "Customer flagged for AML review.");
        return "redirect:/compliance/dashboard";
    }

    @PostMapping("/documents/{id}/verify")
    public String verifyDocument(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        CustomerDocument doc = customerDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        doc.setVerified(true);
        doc.setVerifiedBy(userService.getByEmail(authentication.getName()));
        doc.setVerifiedAt(LocalDateTime.now());
        customerDocumentRepository.save(doc);
        auditLogService.log(authentication.getName(), "DOCUMENT_VERIFIED", "CustomerDocument", id, doc.getDocumentType());
        ra.addFlashAttribute("success", "Document verified.");
        return "redirect:/compliance/dashboard";
    }
}
