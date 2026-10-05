
package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Customer;
import com.lankatrust.smartbank.entity.CustomerDocument;
import com.lankatrust.smartbank.entity.NotificationChannel;
import com.lankatrust.smartbank.entity.NotificationTemplate;
import com.lankatrust.smartbank.entity.Role;
import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.repository.BranchRepository;
import com.lankatrust.smartbank.repository.CustomerDocumentRepository;
import com.lankatrust.smartbank.repository.CustomerRepository;
import com.lankatrust.smartbank.repository.NotificationTemplateRepository;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.ApprovalService;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.FileStorageService;
import com.lankatrust.smartbank.service.TransactionService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/officer")
@PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER')")
public class OfficerController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CustomerDocumentRepository customerDocumentRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final ApprovalService approvalService;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("customers", userService.searchCustomers(q));
        model.addAttribute("branches", branchRepository.findAll());
        model.addAttribute("query", q);
        return "officer/customers";
    }

    @PostMapping("/customers/create")
    public String createCustomer(@RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam String nic,
                                 @RequestParam String address,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
                                 @RequestParam(required = false) Integer branchId,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        try {
            String custId = "CUST" + System.currentTimeMillis();
            var branch = branchId != null ? branchRepository.findById(branchId).orElse(null) : null;
            Customer customer = Customer.builder()
                    .fullName(fullName.trim())
                    .email(email.trim().toLowerCase())
                    .phoneNumber(phone.trim())
                    .nic(nic.trim().toUpperCase())
                    .dob(dob)
                    .address(address.trim())
                    .customerId(custId)
                    .kycStatus("PENDING")
                    .branch(branch)
                    .role(Role.CUSTOMER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            Customer saved = customerRepository.save(customer);
            auditLogService.log(authentication.getName(), "CUSTOMER_CREATED", "Customer", saved.getId(),
                    "Created customer record for " + fullName + " (" + custId + ")");
            ra.addFlashAttribute("success", "Customer record created successfully: " + custId);
            return "redirect:/officer/customers/" + saved.getId();
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/officer/customers";
        }
    }

    @PostMapping("/customers/{id}/upload-document")
    public String uploadDocument(@PathVariable Long id,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam String documentType,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (!(user instanceof Customer customer)) {
                throw new IllegalArgumentException("Target user is not a customer.");
            }
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Please select a file to upload.");
            }
            String storedPath = fileStorageService.store(file, "documents");
            CustomerDocument doc = CustomerDocument.builder()
                    .customer(customer)
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .filePath(storedPath)
                    .uploadedAt(LocalDateTime.now())
                    .verified(false)
                    .build();
            customerDocumentRepository.save(doc);
            auditLogService.log(authentication.getName(), "DOCUMENT_UPLOADED", "CustomerDocument", doc.getId(),
                    documentType + " uploaded for customer " + customer.getEmail());
            ra.addFlashAttribute("success", "Document uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/customers/" + id;
    }

    @GetMapping("/customers/{id}")
    public String customerDetail(@PathVariable Long id, Model model) {
        User customer = userService.getById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("address", customer instanceof com.lankatrust.smartbank.entity.Customer c ? c.getAddress() : "");
        model.addAttribute("accounts", accountService.getForCustomer(id));
        model.addAttribute("documents", customerDocumentRepository.findByCustomerId(id));
        return "officer/customer-detail";
    }

    /** U (request): Officer proposes an update to a customer's record — Manager approval required before it is applied. */
    @PostMapping("/customers/{id}/request-update")
    public String requestCustomerUpdate(@PathVariable Long id,
                                        @RequestParam String fullName,
                                        @RequestParam String phone,
                                        @RequestParam(required = false) String address,
                                        Authentication authentication,
                                        RedirectAttributes ra) {
        try {
            User customer = userService.getById(id);
            // Proposed values are packed into remarks (same convention already used for TEMPLATE_UPDATE)
            // and unpacked by ApprovalServiceImpl on manager approval — no change is applied yet.
            String proposed = "fullName=" + fullName + "|phone=" + phone + "|address=" + (address != null ? address : "");
            approvalService.create("CUSTOMER_RECORD_UPDATE", id, customer.getEmail(), authentication.getName(), proposed);
            ra.addFlashAttribute("success", "Customer record update submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/customers/" + id;
    }

    /** D (request): Officer requests deletion of an outdated customer record — Manager approval required. */
    @PostMapping("/customers/{id}/request-deletion")
    public String requestCustomerDeletion(@PathVariable Long id, @RequestParam(required = false) String reason,
                                          Authentication authentication, RedirectAttributes ra) {
        try {
            User customer = userService.getById(id);
            approvalService.create("CUSTOMER_RECORD_DELETION", id, customer.getEmail(), authentication.getName(),
                    reason != null ? reason : "Outdated record — deletion requested by officer");
            ra.addFlashAttribute("success", "Customer record deletion request submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/customers/" + id;
    }

    @GetMapping("/transfers")
    public String transfers(Model model) {
        model.addAttribute("pending", transactionService.getPendingTransfers());
        model.addAttribute("recent", transactionService.getRecentTransactions(25));
        return "officer/transfers";
    }

    @PostMapping("/transfers/{id}/update")
    public String updateTransfer(@PathVariable Long id,
                                 @RequestParam(required = false) BigDecimal amount,
                                 @RequestParam(required = false) String remarks,
                                 @RequestParam(required = false) Long destinationAccountId,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        try {
            transactionService.updatePendingTransfer(id, amount, remarks, destinationAccountId, authentication.getName());
            ra.addFlashAttribute("success", "Pending transfer updated successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/transfers";
    }

    @PostMapping("/transfers/{id}/approve")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String approveTransfer(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            transactionService.approveAndExecuteTransfer(id, authentication.getName());
            ra.addFlashAttribute("success", "Transfer approved and executed successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/transfers";
    }

    @PostMapping("/transfers/{id}/cancel")
    public String cancelTransfer(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            transactionService.cancelPendingTransfer(id, authentication.getName());
            ra.addFlashAttribute("success", "Pending transfer cancelled.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/transfers";
    }

    @GetMapping("/corrections")
    public String corrections(Model model) {
        model.addAttribute("transactions", transactionService.getRecentTransactions(50));
        model.addAttribute("corrections", transactionService.getCorrections());
        return "officer/corrections";
    }

    @PostMapping("/corrections")
    public String submitCorrection(@RequestParam Long transactionId,
                                   @RequestParam String reason,
                                   @RequestParam(required = false) String proposedRemarks,
                                   @RequestParam(required = false) BigDecimal proposedAmount,
                                   Authentication authentication,
                                   RedirectAttributes ra) {
        try {
            transactionService.requestCorrection(transactionId, reason, proposedRemarks, proposedAmount, authentication.getName());
            ra.addFlashAttribute("success", "Correction request submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/corrections";
    }

    @PostMapping("/corrections/request-delete")
    public String requestTxnDeletion(@RequestParam Long transactionId,
                                     @RequestParam String reason,
                                     Authentication authentication,
                                     RedirectAttributes ra) {
        try {
            transactionService.requestTransactionDeletion(transactionId, reason, authentication.getName());
            ra.addFlashAttribute("success", "Transaction deletion request submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/corrections";
    }

    @GetMapping("/templates")
    public String templates(Model model) {
        if (notificationTemplateRepository.count() == 0) {
            seedTemplates();
        }
        model.addAttribute("templates", notificationTemplateRepository.findAll());
        return "officer/templates";
    }

    @PostMapping("/templates/create")
    public String createTemplate(@RequestParam String templateKey,
                                 @RequestParam String name,
                                 @RequestParam String channel,
                                 @RequestParam String subject,
                                 @RequestParam String body,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        try {
            String proposed = "templateKey=" + templateKey + "|name=" + name + "|channel=" + channel
                    + "|subject=" + subject + "|body=" + body;
            approvalService.create("TEMPLATE_CREATE", 0L, templateKey, authentication.getName(), proposed);
            ra.addFlashAttribute("success", "Template creation request submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/templates";
    }

    @PostMapping("/templates/{id}")
    public String updateTemplate(@PathVariable Long id,
                                 @RequestParam String subject,
                                 @RequestParam String body,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        try {
            NotificationTemplate template = notificationTemplateRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
            String proposed = "subject=" + subject + "|body=" + body;
            approvalService.create("TEMPLATE_UPDATE", id, template.getTemplateKey(), authentication.getName(), proposed);
            ra.addFlashAttribute("success", "Template update request submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/templates";
    }

    @PostMapping("/templates/{id}/delete")
    public String deleteTemplate(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        try {
            NotificationTemplate template = notificationTemplateRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
            approvalService.create("TEMPLATE_DELETE", id, template.getTemplateKey(), authentication.getName(),
                    "Deletion requested by " + authentication.getName());
            ra.addFlashAttribute("success", "Template deletion request submitted for manager approval.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/officer/templates";
    }

    @GetMapping("/reports")
    public String reports() {
        return "officer/reports";
    }

    private void seedTemplates() {
        notificationTemplateRepository.save(NotificationTemplate.builder()
                .templateKey("ACCOUNT_ACTIVATED").name("Account Activated").subject("Your account is active")
                .body("Your LankaTrust account has been activated.").channel(NotificationChannel.EMAIL).build());
        notificationTemplateRepository.save(NotificationTemplate.builder()
                .templateKey("TRANSFER_SUCCESS").name("Transfer Success").subject("Transfer completed")
                .body("Your fund transfer has been completed.").channel(NotificationChannel.EMAIL).build());
        notificationTemplateRepository.save(NotificationTemplate.builder()
                .templateKey("LOAN_UPDATE").name("Loan Update").subject("Loan application update")
                .body("There is an update to your loan application.").channel(NotificationChannel.IN_APP).build());
    }
}

