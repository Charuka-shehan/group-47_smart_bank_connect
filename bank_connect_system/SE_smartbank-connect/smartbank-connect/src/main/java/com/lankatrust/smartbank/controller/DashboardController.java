package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.entity.User;
import com.lankatrust.smartbank.service.*;
import com.lankatrust.smartbank.dto.UserDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final AtmCardService atmCardService;
    private final ApprovalService approvalService;

    public DashboardController(UserService userService,
                               NotificationService notificationService,
                               AccountService accountService,
                               TransactionService transactionService,
                               LoanService loanService,
                               AtmCardService atmCardService,
                               ApprovalService approvalService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.loanService = loanService;
        this.atmCardService = atmCardService;
        this.approvalService = approvalService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        return "redirect:/bankdash/overview";
    }

    @GetMapping("/customer/dashboard")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BANK_MANAGER')")
    public String customerDashboard(Authentication authentication, Model model) {
        populateDashboardAttributes(authentication, model);
        User user = userService.getByEmail(authentication.getName());
        var accounts = accountService.getForCustomer(user.getId());
        if (accounts.isEmpty() || user.getRole() == com.lankatrust.smartbank.entity.Role.BANK_MANAGER || user.getRole() == com.lankatrust.smartbank.entity.Role.SYSTEM_ADMINISTRATOR) {
            accounts = accountService.getAll();
        }
        model.addAttribute("accounts", accounts);

        java.util.List<com.lankatrust.smartbank.entity.Transaction> recent = new java.util.ArrayList<>();
        for (var acc : accounts) {
            recent.addAll(transactionService.historyForAccount(acc.getId()));
        }
        recent.sort(java.util.Comparator.comparing(com.lankatrust.smartbank.entity.Transaction::getCreatedAt).reversed());
        model.addAttribute("recentTransactions", recent.stream().limit(5).toList());

        model.addAttribute("loans", loanService.getForCustomer(user.getId()));
        model.addAttribute("notifications", notificationService.getForUser(user.getId()));

        if (!accounts.isEmpty()) {
            atmCardService.findByAccountId(accounts.get(0).getId()).ifPresent(card -> model.addAttribute("atmCard", card));
        }
        return "customer-dashboard";
    }

    @GetMapping("/officer/dashboard")
    @PreAuthorize("hasAnyRole('BANK_OFFICER', 'BANK_MANAGER')")
    public String officerDashboard(Authentication authentication, Model model) {
        populateDashboardAttributes(authentication, model);
        model.addAttribute("pendingAccounts", accountService.getAll().stream()
                .filter(a -> a.getStatus() == com.lankatrust.smartbank.entity.AccountStatus.PENDING_APPROVAL)
                .toList());
        model.addAttribute("recentTransactions", transactionService.getRecentTransactions(10));
        return "officer-dashboard";
    }

    @GetMapping("/customer-relations/dashboard")
    @PreAuthorize("hasAnyRole('CUSTOMER_RELATIONS_EXECUTIVE', 'BANK_MANAGER')")
    public String relationsDashboard() {
        return "redirect:/cre/dashboard";
    }

    @GetMapping("/manager/dashboard")
    @PreAuthorize("hasRole('BANK_MANAGER')")
    public String managerDashboard(Authentication authentication, Model model) {
        populateDashboardAttributes(authentication, model);
        var allAccounts = accountService.getAll();
        var allLoans = loanService.getAll();
        var allTxns = transactionService.getAll();
        var allCustomers = userService.findByRole(com.lankatrust.smartbank.entity.Role.CUSTOMER);
        var allStaff = userService.findAll().stream()
                .filter(u -> u.getRole() != com.lankatrust.smartbank.entity.Role.CUSTOMER)
                .toList();

        // 1. Core KPIs
        model.addAttribute("totalAccounts", allAccounts.size());
        model.addAttribute("totalCustomers", allCustomers.size());
        model.addAttribute("totalStaff", allStaff.size());

        java.math.BigDecimal totalBalance = allAccounts.stream()
                .map(com.lankatrust.smartbank.entity.Account::getBalance)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("totalBalance", totalBalance);

        // Pending items
        var pendingAccountsList = allAccounts.stream()
                .filter(a -> a.getStatus() == com.lankatrust.smartbank.entity.AccountStatus.PENDING_APPROVAL)
                .toList();
        model.addAttribute("pendingCount", pendingAccountsList.size());
        model.addAttribute("pendingAccountsList", pendingAccountsList.stream().limit(5).toList());

        var pendingLoansList = allLoans.stream()
                .filter(l -> l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.APPROVED
                        && l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.REJECTED
                        && l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.CANCELLED)
                .toList();
        model.addAttribute("pendingLoans", pendingLoansList.size());
        model.addAttribute("pendingLoansList", pendingLoansList.stream().limit(5).toList());

        var pendingApprovals = approvalService.getPending();
        model.addAttribute("pendingApprovalsCount", pendingApprovals.size());
        model.addAttribute("pendingApprovalsList", pendingApprovals.stream().limit(5).toList());

        // Account type breakdown
        long savingsCount = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("SAVINGS")).count();
        long currentCount = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("CURRENT")).count();
        long fdCount = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("FIXED_DEPOSIT")).count();
        model.addAttribute("savingsCount", savingsCount);
        model.addAttribute("currentCount", currentCount);
        model.addAttribute("fdCount", fdCount);

        // Loan portfolio stats
        long loanApproved = allLoans.stream().filter(l -> l.getStatus() == com.lankatrust.smartbank.entity.LoanStatus.APPROVED).count();
        long loanRejected = allLoans.stream().filter(l -> l.getStatus() == com.lankatrust.smartbank.entity.LoanStatus.REJECTED).count();
        long loanPending = pendingLoansList.size();
        model.addAttribute("loanApprovedCount", loanApproved);
        model.addAttribute("loanRejectedCount", loanRejected);
        model.addAttribute("loanPendingCount", loanPending);

        java.math.BigDecimal totalLoanAmount = allLoans.stream()
                .map(com.lankatrust.smartbank.entity.Loan::getRequestedAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("totalLoanAmount", totalLoanAmount);

        // 24h Transactions (Today)
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.MIN);
        long todayTxnsCount = allTxns.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(startOfDay))
                .count();
        model.addAttribute("todayTransactions", todayTxnsCount);

        java.math.BigDecimal todayVolume = allTxns.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(startOfDay)
                        && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED)
                .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("todayVolume", todayVolume);

        // Recent Transactions (top 8)
        model.addAttribute("recentTransactions", transactionService.getRecentTransactions(8));

        // 2. Chart.js Series Data
        buildManagerChartData(model, allTxns, allAccounts, allLoans);

        return "manager-dashboard";
    }

    @GetMapping("/manager/api/chart-data")
    @PreAuthorize("hasRole('BANK_MANAGER')")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> managerChartData() {
        var allAccounts = accountService.getAll();
        var allLoans = loanService.getAll();
        var allTxns = transactionService.getAll();
        var allCustomers = userService.findByRole(com.lankatrust.smartbank.entity.Role.CUSTOMER);

        java.util.Map<String, Object> data = new java.util.HashMap<>();

        // 7-day transaction velocity
        java.util.List<String> dayLabels = new java.util.ArrayList<>();
        java.util.List<Long> dayCounts = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> dayVolumes = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter dayFmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM");

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
            long count = allTxns.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(day))
                    .count();
            java.math.BigDecimal vol = allTxns.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(day)
                            && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED)
                    .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            dayLabels.add(day.format(dayFmt));
            dayCounts.add(count);
            dayVolumes.add(vol);
        }
        data.put("dailyTxnLabels", dayLabels);
        data.put("dailyTxnCounts", dayCounts);
        data.put("dailyTxnVolumes", dayVolumes);

        // 6-month monthly cash flow
        java.util.List<String> monthLabels = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> monthlyDeposits = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> monthlyWithdrawals = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter monthFmt = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");

        java.math.BigDecimal totalDeposits = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalWithdrawals = java.math.BigDecimal.ZERO;

        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = java.time.YearMonth.now().minusMonths(i);
            monthLabels.add(ym.format(monthFmt));

            java.math.BigDecimal dep = allTxns.stream()
                    .filter(t -> t.getType() == com.lankatrust.smartbank.entity.TransactionType.DEPOSIT
                            && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED
                            && t.getCreatedAt() != null && java.time.YearMonth.from(t.getCreatedAt()).equals(ym))
                    .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            monthlyDeposits.add(dep);
            totalDeposits = totalDeposits.add(dep);

            java.math.BigDecimal wdr = allTxns.stream()
                    .filter(t -> t.getType() == com.lankatrust.smartbank.entity.TransactionType.WITHDRAWAL
                            && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED
                            && t.getCreatedAt() != null && java.time.YearMonth.from(t.getCreatedAt()).equals(ym))
                    .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            monthlyWithdrawals.add(wdr);
            totalWithdrawals = totalWithdrawals.add(wdr);
        }
        data.put("monthLabels", monthLabels);
        data.put("monthlyDepositData", monthlyDeposits);
        data.put("monthlyWithdrawalData", monthlyWithdrawals);
        data.put("isNetPositiveLiquidity", totalDeposits.compareTo(totalWithdrawals) >= 0);

        // Account Portfolio breakdown
        long savings = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("SAVINGS")).count();
        long current = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("CURRENT")).count();
        long fd = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("FIXED_DEPOSIT")).count();
        data.put("accountTypeDistribution", java.util.List.of(savings, current, fd));
        data.put("savingsCount", savings);
        data.put("currentCount", current);
        data.put("fdCount", fd);

        // Loan Portfolio breakdown
        long approvedLoans = allLoans.stream().filter(l -> l.getStatus() == com.lankatrust.smartbank.entity.LoanStatus.APPROVED).count();
        long pendingLoans = allLoans.stream().filter(l -> l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.APPROVED
                && l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.REJECTED
                && l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.CANCELLED).count();
        long rejectedLoans = allLoans.stream().filter(l -> l.getStatus() == com.lankatrust.smartbank.entity.LoanStatus.REJECTED).count();
        data.put("loanStatusDistribution", java.util.List.of(approvedLoans, pendingLoans, rejectedLoans));
        data.put("loanApprovedCount", approvedLoans);
        data.put("loanPendingCount", pendingLoans);
        data.put("loanRejectedCount", rejectedLoans);

        // Real-time KPIs
        java.math.BigDecimal totalBalance = allAccounts.stream()
                .map(com.lankatrust.smartbank.entity.Account::getBalance)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        data.put("totalBalance", totalBalance);
        data.put("totalAccounts", allAccounts.size());
        data.put("totalCustomers", allCustomers.size());

        long pendingAccounts = allAccounts.stream()
                .filter(a -> a.getStatus() == com.lankatrust.smartbank.entity.AccountStatus.PENDING_APPROVAL)
                .count();
        data.put("pendingAccounts", pendingAccounts);
        data.put("pendingLoans", pendingLoans);

        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.MIN);
        long todayTxnsCount = allTxns.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(startOfDay))
                .count();
        java.math.BigDecimal todayVolume = allTxns.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(startOfDay)
                        && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED)
                .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        data.put("todayTransactions", todayTxnsCount);
        data.put("todayVolume", todayVolume);

        return org.springframework.http.ResponseEntity.ok(data);
    }

    private void buildManagerChartData(Model model,
                                       java.util.List<com.lankatrust.smartbank.entity.Transaction> allTxns,
                                       java.util.List<com.lankatrust.smartbank.entity.Account> allAccounts,
                                       java.util.List<com.lankatrust.smartbank.entity.Loan> allLoans) {
        // Daily transaction volume (last 7 days)
        java.util.List<String> dayLabels = new java.util.ArrayList<>();
        java.util.List<Long> dayCounts = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> dayVolumes = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter dayFmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM");

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
            long count = allTxns.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(day))
                    .count();
            java.math.BigDecimal vol = allTxns.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(day)
                            && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED)
                    .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            dayLabels.add(day.format(dayFmt));
            dayCounts.add(count);
            dayVolumes.add(vol);
        }
        model.addAttribute("dailyTxnLabels", toJsStringArray(dayLabels));
        model.addAttribute("dailyTxnCounts", toJsNumberArray(dayCounts));
        model.addAttribute("dailyTxnVolumes", toJsDecimalArray(dayVolumes));

        // Monthly cash flow: deposits vs withdrawals (last 6 months)
        java.util.List<String> monthLabels = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> monthlyDeposits = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> monthlyWithdrawals = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter monthFmt = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");

        java.math.BigDecimal totalDeposits = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalWithdrawals = java.math.BigDecimal.ZERO;

        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = java.time.YearMonth.now().minusMonths(i);
            monthLabels.add(ym.format(monthFmt));

            java.math.BigDecimal dep = allTxns.stream()
                    .filter(t -> t.getType() == com.lankatrust.smartbank.entity.TransactionType.DEPOSIT
                            && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED
                            && t.getCreatedAt() != null && java.time.YearMonth.from(t.getCreatedAt()).equals(ym))
                    .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            monthlyDeposits.add(dep);
            totalDeposits = totalDeposits.add(dep);

            java.math.BigDecimal wdr = allTxns.stream()
                    .filter(t -> t.getType() == com.lankatrust.smartbank.entity.TransactionType.WITHDRAWAL
                            && t.getStatus() == com.lankatrust.smartbank.entity.TransactionStatus.COMPLETED
                            && t.getCreatedAt() != null && java.time.YearMonth.from(t.getCreatedAt()).equals(ym))
                    .map(com.lankatrust.smartbank.entity.Transaction::getAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            monthlyWithdrawals.add(wdr);
            totalWithdrawals = totalWithdrawals.add(wdr);
        }
        model.addAttribute("monthLabels", toJsStringArray(monthLabels));
        model.addAttribute("monthlyDepositData", toJsDecimalArray(monthlyDeposits));
        model.addAttribute("monthlyWithdrawalData", toJsDecimalArray(monthlyWithdrawals));
        model.addAttribute("isNetPositiveLiquidity", totalDeposits.compareTo(totalWithdrawals) >= 0);

        // Account Portfolio breakdown
        long savings = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("SAVINGS")).count();
        long current = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("CURRENT")).count();
        long fd = allAccounts.stream().filter(a -> a.getAccountType() != null && a.getAccountType().trim().equalsIgnoreCase("FIXED_DEPOSIT")).count();
        model.addAttribute("accountTypeDistribution", "[" + savings + "," + current + "," + fd + "]");

        // Loan Portfolio breakdown
        long approvedLoans = allLoans.stream().filter(l -> l.getStatus() == com.lankatrust.smartbank.entity.LoanStatus.APPROVED).count();
        long pendingLoans = allLoans.stream().filter(l -> l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.APPROVED
                && l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.REJECTED
                && l.getStatus() != com.lankatrust.smartbank.entity.LoanStatus.CANCELLED).count();
        long rejectedLoans = allLoans.stream().filter(l -> l.getStatus() == com.lankatrust.smartbank.entity.LoanStatus.REJECTED).count();
        model.addAttribute("loanStatusDistribution", "[" + approvedLoans + "," + pendingLoans + "," + rejectedLoans + "]");
    }

    private String toJsStringArray(java.util.List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(values.get(i).replace("\"", "")).append("\"");
        }
        return sb.append("]").toString();
    }

    private String toJsNumberArray(java.util.List<Long> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(values.get(i));
        }
        return sb.append("]").toString();
    }

    private String toJsDecimalArray(java.util.List<java.math.BigDecimal> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(values.get(i).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        }
        return sb.append("]").toString();
    }

    private void populateDashboardAttributes(Authentication authentication, Model model) {
        User user = userService.getByEmail(authentication.getName());
        UserDto dto = UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .nic(user.getNic())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .enabled(user.isEnabled())
                .googleLinked(user.isGoogleLinked())
                .lastLogin(user.getLastLogin())
                .build();
        model.addAttribute("userDto", dto);
        model.addAttribute("unreadCount", notificationService.unreadCount(user.getId()));
        model.addAttribute("dashboardRole", user.getRole() != null ? user.getRole().name() : "");
    }
}
