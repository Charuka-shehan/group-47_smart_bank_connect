package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.Account;
import com.lankatrust.smartbank.entity.AuditLog;
import com.lankatrust.smartbank.entity.PublishedReport;
import com.lankatrust.smartbank.entity.Transaction;
import com.lankatrust.smartbank.repository.ApprovalRequestRepository;
import com.lankatrust.smartbank.repository.AuditLogRepository;
import com.lankatrust.smartbank.repository.PublishedReportRepository;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.ApprovalService;
import com.lankatrust.smartbank.service.PdfReportService;
import com.lankatrust.smartbank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final PdfReportService pdfReportService;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AuditLogRepository auditLogRepository;
    private final com.lankatrust.smartbank.repository.TransactionRepository transactionRepository;
    private final com.lankatrust.smartbank.service.LoanService loanService;
    private final com.lankatrust.smartbank.service.UserService userService;
    private final PublishedReportRepository publishedReportRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalService approvalService;

    /** Customer Statement PDF Download */
    @GetMapping("/statement/download")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_MANAGER')")
    public ResponseEntity<InputStreamResource> downloadStatement(Authentication authentication) {
        String email = authentication.getName();
        // Retrieve all accounts for customer
        List<Account> accounts = accountService.getAll().stream()
                .filter(acc -> acc.getCustomer().getEmail().equals(email))
                .collect(Collectors.toList());

        List<Transaction> transactions = new ArrayList<>();
        for (Account acc : accounts) {
            transactions.addAll(transactionService.historyForAccount(acc.getId()));
        }

        ByteArrayInputStream bis = pdfReportService.generateTransactionReport(transactions, "SmartBank Connect - Account Statement");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=statement.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    /** Daily Transactions Report (Staff/Manager/Admin) */
    @GetMapping("/transactions/daily")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> dailyTransactionsReport() {
        List<Transaction> transactions = transactionRepository.findAll();
        ByteArrayInputStream bis = pdfReportService.generateTransactionReport(transactions, "Daily Transactions Summary");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=daily_transactions.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    /** Active Accounts Report (Staff/Manager/Admin) */
    @GetMapping("/accounts/active")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> activeAccountsReport() {
        List<Account> activeAccounts = accountService.getAll().stream()
                .filter(acc -> "ACTIVE".equalsIgnoreCase(acc.getStatus().toString()))
                .collect(Collectors.toList());

        ByteArrayInputStream bis = pdfReportService.generateAccountReport(activeAccounts, "Active Accounts Report");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=active_accounts.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    /** Frozen Accounts Report (Staff/Manager/Admin) */
    @GetMapping("/accounts/frozen")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> frozenAccountsReport() {
        List<Account> frozenAccounts = accountService.getAll().stream()
                .filter(acc -> "FROZEN".equalsIgnoreCase(acc.getStatus().toString()))
                .collect(Collectors.toList());

        ByteArrayInputStream bis = pdfReportService.generateAccountReport(frozenAccounts, "Frozen Accounts Report");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=frozen_accounts.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    /** Audit Logs Report (Manager/Admin Only) */
    @GetMapping("/audit/logs")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> auditLogsReport() {
        List<AuditLog> logs = auditLogRepository.findAll();
        ByteArrayInputStream bis = pdfReportService.generateAuditLogReport(logs, "System Audit Log Report");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=audit_logs.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    /* =====================================================================
       CSV EXPORTS (Section 8: Reporting System — PDF and CSV, downloadable)
       ===================================================================== */

    @GetMapping("/transactions/daily/csv")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<byte[]> dailyTransactionsCsv() {
        List<Transaction> transactions = transactionRepository.findAll();
        StringBuilder csv = new StringBuilder("Reference,Type,Amount (LKR),Status,Source Account,Destination Account,Created At\n");
        for (Transaction t : transactions) {
            csv.append(csvCell(t.getReferenceNumber())).append(",")
               .append(csvCell(t.getType() != null ? t.getType().toString() : "")).append(",")
               .append(csvCell(t.getAmount() != null ? t.getAmount().toPlainString() : "")).append(",")
               .append(csvCell(t.getStatus() != null ? t.getStatus().toString() : "")).append(",")
               .append(csvCell(t.getSourceAccount() != null ? t.getSourceAccount().getAccountNumber() : "")).append(",")
               .append(csvCell(t.getDestinationAccount() != null ? t.getDestinationAccount().getAccountNumber() : "")).append(",")
               .append(csvCell(t.getCreatedAt() != null ? t.getCreatedAt().toString() : "")).append("\n");
        }
        return csvResponse(csv.toString(), "daily_transactions.csv");
    }

    @GetMapping("/accounts/active/csv")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<byte[]> activeAccountsCsv() {
        List<Account> activeAccounts = accountService.getAll().stream()
                .filter(acc -> "ACTIVE".equalsIgnoreCase(acc.getStatus().toString()))
                .collect(Collectors.toList());
        return csvResponse(accountsToCsv(activeAccounts), "active_accounts.csv");
    }

    @GetMapping("/accounts/frozen/csv")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<byte[]> frozenAccountsCsv() {
        List<Account> frozenAccounts = accountService.getAll().stream()
                .filter(acc -> "FROZEN".equalsIgnoreCase(acc.getStatus().toString()))
                .collect(Collectors.toList());
        return csvResponse(accountsToCsv(frozenAccounts), "frozen_accounts.csv");
    }

    @GetMapping("/audit/logs/csv")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<byte[]> auditLogsCsv() {
        List<AuditLog> logs = auditLogRepository.findAll();
        StringBuilder csv = new StringBuilder("ID,Actor,Action,Entity,Details,Timestamp\n");
        for (AuditLog log : logs) {
            csv.append(csvCell(String.valueOf(log.getId()))).append(",")
               .append(csvCell(log.getActorEmail() != null ? log.getActorEmail() : "SYSTEM")).append(",")
               .append(csvCell(log.getAction() != null ? log.getAction() : "")).append(",")
               .append(csvCell(log.getEntityType() != null ? log.getEntityType() + " #" + log.getEntityId() : "")).append(",")
               .append(csvCell(log.getDetails() != null ? log.getDetails() : "")).append(",")
               .append(csvCell(log.getTimestamp() != null ? log.getTimestamp().toString() : "")).append("\n");
        }
        return csvResponse(csv.toString(), "audit_logs.csv");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String reportsHub() {
        return "officer/reports";
    }

    /* =====================================================================
       REPORTING WORKFLOW (Section 4/6: Officer creates & updates for review;
       Manager approves distribution; outdated reports archived on approval)
       ===================================================================== */

    /** R: Officer/Manager/Admin view all draft, pending, published and archived reports. */
    @GetMapping({"/manage", "/draft"})
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String manageReports(Model model) {
        model.addAttribute("reports", publishedReportRepository.findAllByOrderByCreatedAtDesc());
        return "officer/reports-manage";
    }

    /** C: Officer creates a new report draft. No approval needed to draft — only to publish/distribute. */
    @PostMapping({"/draft", "/manage/draft"})
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String createDraft(@RequestParam String name, @RequestParam String type,
                              @RequestParam(required = false) String params,
                              Authentication authentication, RedirectAttributes ra) {
        String cleanParams = (params != null && !params.trim().isEmpty()) ? params.trim() : null;
        PublishedReport report = PublishedReport.builder()
                .name(name.trim())
                .type(type.trim())
                .params(cleanParams)
                .generatedBy(userService.getByEmail(authentication.getName()))
                .status("DRAFT")
                .build();
        publishedReportRepository.save(report);
        ra.addFlashAttribute("success", "Report draft created successfully.");
        return "redirect:/reports/draft";
    }

    /** U: Officer/Manager updates a draft before publication. */
    @PostMapping({"/{id}/update", "/draft/{id}/update"})
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String updateDraft(@PathVariable Long id, @RequestParam String name, @RequestParam String type,
                              @RequestParam(required = false) String params, RedirectAttributes ra) {
        PublishedReport report = publishedReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (!"DRAFT".equals(report.getStatus()) && !"REJECTED".equals(report.getStatus())) {
            ra.addFlashAttribute("error", "Only draft or rejected reports can be edited.");
            return "redirect:/reports/draft";
        }
        String cleanParams = (params != null && !params.trim().isEmpty()) ? params.trim() : null;
        report.setName(name.trim());
        report.setType(type.trim());
        report.setParams(cleanParams);
        report.setStatus("DRAFT");
        publishedReportRepository.save(report);
        ra.addFlashAttribute("success", "Report draft updated.");
        return "redirect:/reports/draft";
    }

    /** Officer requests distribution — goes to Manager approval queue. */
    @PostMapping({"/{id}/submit-publish", "/draft/{id}/submit-publish"})
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String submitForPublish(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        PublishedReport report = publishedReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        report.setStatus("PENDING_PUBLISH");
        publishedReportRepository.save(report);
        approvalService.create("REPORT_PUBLISH", id, report.getName(), authentication.getName(),
                "Requesting distribution of report: " + report.getName());
        ra.addFlashAttribute("success", "Report submitted for manager approval before distribution.");
        return "redirect:/reports/draft";
    }

    /** Manager/Admin directly publishes a report draft without creating duplicate records. */
    @PostMapping({"/{id}/publish", "/draft/{id}/publish"})
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String publishDraft(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        PublishedReport report = publishedReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if ("PUBLISHED".equals(report.getStatus())) {
            ra.addFlashAttribute("success", "Report is already published.");
            return "redirect:/reports/draft";
        }
        report.setStatus("PUBLISHED");
        publishedReportRepository.save(report);

        // If there was a pending approval request for this report, resolve it cleanly
        var pendingApprovals = approvalRequestRepository.findByTargetId(id).stream()
                .filter(req -> "REPORT_PUBLISH".equals(req.getRequestType()) && "PENDING".equals(req.getStatus()))
                .toList();
        for (var req : pendingApprovals) {
            req.setStatus("APPROVED");
            req.setApprovedBy(userService.getByEmail(authentication.getName()));
            req.setApprovedAt(java.time.LocalDateTime.now());
            approvalRequestRepository.save(req);
        }

        ra.addFlashAttribute("success", "Report published successfully.");
        return "redirect:/reports/draft";
    }

    /** D: Directly deletes a report draft from the database. */
    @PostMapping({"/{id}/delete", "/draft/{id}/delete"})
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String deleteDraft(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        PublishedReport report = publishedReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        var relatedApprovals = approvalRequestRepository.findByTargetId(id).stream()
                .filter(req -> req.getRequestType() != null && req.getRequestType().startsWith("REPORT_"))
                .toList();
        if (!relatedApprovals.isEmpty()) {
            approvalRequestRepository.deleteAll(relatedApprovals);
        }
        publishedReportRepository.delete(report);
        ra.addFlashAttribute("success", "Report deleted successfully from database.");
        return "redirect:/reports/draft";
    }

    @DeleteMapping({"/{id}", "/draft/{id}"})
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> deleteDraftApi(@PathVariable Long id) {
        try {
            PublishedReport report = publishedReportRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));
            var relatedApprovals = approvalRequestRepository.findByTargetId(id).stream()
                    .filter(req -> req.getRequestType() != null && req.getRequestType().startsWith("REPORT_"))
                    .toList();
            if (!relatedApprovals.isEmpty()) {
                approvalRequestRepository.deleteAll(relatedApprovals);
            }
            publishedReportRepository.delete(report);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Report deleted successfully from database."));
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    /** D (functionally: archive) — Officer/Manager requests an outdated report be archived; needs manager approval. */
    @PostMapping("/{id}/request-archive")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String requestArchive(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        PublishedReport report = publishedReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        approvalService.create("REPORT_ARCHIVE", id, report.getName(), authentication.getName(),
                "Requesting archive of outdated report: " + report.getName());
        ra.addFlashAttribute("success", "Archive request submitted for manager approval.");
        return "redirect:/reports/draft";
    }

    @PostMapping("/{id}/request-delete")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public String requestDelete(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        PublishedReport report = publishedReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        approvalService.create("REPORT_DELETE", id, report.getName(), authentication.getName(),
                "Requesting deletion of report: " + report.getName());
        ra.addFlashAttribute("success", "Deletion request submitted for manager approval.");
        return "redirect:/reports/draft";
    }

    @GetMapping("/transactions/weekly")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> weeklyTransactionsReport() {
        var start = java.time.LocalDateTime.now().minusDays(7);
        List<Transaction> transactions = transactionService.findBetween(start, java.time.LocalDateTime.now());
        ByteArrayInputStream bis = pdfReportService.generateTransactionReport(transactions, "Weekly Transactions Summary");
        return pdfResponse(bis, "weekly_transactions.pdf");
    }

    @GetMapping("/transactions/monthly")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> monthlyTransactionsReport() {
        var start = java.time.LocalDateTime.now().minusDays(30);
        List<Transaction> transactions = transactionService.findBetween(start, java.time.LocalDateTime.now());
        ByteArrayInputStream bis = pdfReportService.generateTransactionReport(transactions, "Monthly Transactions Summary");
        return pdfResponse(bis, "monthly_transactions.pdf");
    }

    @GetMapping("/loans/status")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    public ResponseEntity<InputStreamResource> loanStatusReport() {
        List<String[]> rows = loanService.getAll().stream()
                .map(l -> new String[]{
                        l.getApplicationNumber(),
                        l.getCustomer() != null ? l.getCustomer().getFullName() : "",
                        l.getLoanType(),
                        l.getRequestedAmount() != null ? l.getRequestedAmount().toPlainString() : "",
                        l.getStatus() != null ? l.getStatus().name() : ""
                }).toList();
        ByteArrayInputStream bis = pdfReportService.generateTableReport("Loan Approvals / Rejections",
                new String[]{"Application", "Customer", "Type", "Amount", "Status"}, rows);
        return pdfResponse(bis, "loan_status.pdf");
    }

    @GetMapping("/branch/performance")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> branchPerformanceReport() {
        java.util.Map<String, long[]> stats = new java.util.LinkedHashMap<>();
        accountService.getAll().forEach(a -> {
            String name = a.getBranch() != null ? a.getBranch().getName() : "Unassigned";
            stats.putIfAbsent(name, new long[]{0, 0});
            stats.get(name)[0]++;
            if (a.getStatus() != null && "ACTIVE".equals(a.getStatus().name())) {
                stats.get(name)[1]++;
            }
        });
        List<String[]> rows = stats.entrySet().stream()
                .map(e -> new String[]{e.getKey(), String.valueOf(e.getValue()[0]), String.valueOf(e.getValue()[1])})
                .toList();
        ByteArrayInputStream bis = pdfReportService.generateTableReport("Branch Performance",
                new String[]{"Branch", "Total Accounts", "Active Accounts"}, rows);
        return pdfResponse(bis, "branch_performance.pdf");
    }

    @GetMapping("/customers/registrations")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> customerRegistrationsReport() {
        List<String[]> rows = userService.findByRole(com.lankatrust.smartbank.entity.Role.CUSTOMER).stream()
                .map(u -> new String[]{
                        String.valueOf(u.getId()),
                        u.getFullName(),
                        u.getEmail(),
                        u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
                }).toList();
        ByteArrayInputStream bis = pdfReportService.generateTableReport("Customer Registrations",
                new String[]{"ID", "Name", "Email", "Registered"}, rows);
        return pdfResponse(bis, "customer_registrations.pdf");
    }

    @GetMapping("/officer/performance")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> officerPerformanceReport() {
        List<String[]> rows = userService.findByRole(com.lankatrust.smartbank.entity.Role.BANK_OFFICER).stream()
                .map(u -> new String[]{u.getFullName(), u.getEmail(), u.isEnabled() ? "ACTIVE" : "DISABLED"})
                .toList();
        ByteArrayInputStream bis = pdfReportService.generateTableReport("Officer Performance",
                new String[]{"Officer", "Email", "Status"}, rows);
        return pdfResponse(bis, "officer_performance.pdf");
    }

    @GetMapping("/transfers/summary")
    @PreAuthorize("hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InputStreamResource> transferSummaryReport() {
        List<Transaction> transfers = transactionService.getAll().stream()
                .filter(t -> t.getType() == com.lankatrust.smartbank.entity.TransactionType.TRANSFER)
                .toList();
        ByteArrayInputStream bis = pdfReportService.generateTransactionReport(transfers, "Transfer Summary");
        return pdfResponse(bis, "transfer_summary.pdf");
    }

    @GetMapping("/audit-logs/download")
    @PreAuthorize("hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    public ResponseEntity<InputStreamResource> auditLogsAlias() {
        return auditLogsReport();
    }

    private ResponseEntity<InputStreamResource> pdfResponse(ByteArrayInputStream bis, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + filename);
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }

    private String accountsToCsv(List<Account> accounts) {
        StringBuilder csv = new StringBuilder("Account Number,Customer,Type,Balance (LKR),Status,Created At\n");
        for (Account a : accounts) {
            csv.append(csvCell(a.getAccountNumber())).append(",")
               .append(csvCell(a.getCustomer() != null ? a.getCustomer().getFullName() : "")).append(",")
               .append(csvCell(a.getAccountType())).append(",")
               .append(csvCell(a.getBalance() != null ? a.getBalance().toPlainString() : "")).append(",")
               .append(csvCell(a.getStatus() != null ? a.getStatus().toString() : "")).append(",")
               .append(csvCell(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")).append("\n");
        }
        return csv.toString();
    }

    /** Escapes a value for CSV: wraps in quotes and doubles any embedded quotes. */
    private String csvCell(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private ResponseEntity<byte[]> csvResponse(String csvBody, String filename) {
        byte[] bytes = csvBody.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
