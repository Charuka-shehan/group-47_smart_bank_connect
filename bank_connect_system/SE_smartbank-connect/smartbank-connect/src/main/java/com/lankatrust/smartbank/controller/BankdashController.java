package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.dto.UserDto;
import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.AtmCardService;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.TransactionService;
import com.lankatrust.smartbank.service.UserService;
import com.lankatrust.smartbank.service.LoanService;
import com.lankatrust.smartbank.repository.UserRepository;
import com.lankatrust.smartbank.repository.TransactionRepository;
import com.lankatrust.smartbank.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class BankdashController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final AtmCardService atmCardService;

    @GetMapping({"/bankdash", "/bankdash/"})
    public String redirectToOverview() {
        return "redirect:/bankdash/overview";
    }

    @GetMapping("/bankdash/overview")
    public String overview(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            User user = userService.getByEmail(authentication.getName());
            UserDto dto = UserDto.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole() != null ? user.getRole().name() : null)
                    .enabled(user.isEnabled())
                    .googleLinked(user.isGoogleLinked())
                    .lastLogin(user.getLastLogin())
                    .build();
            model.addAttribute("userDto", dto);
            model.addAttribute("unreadCount", notificationService.unreadCount(user.getId()));

            if (user.getRole() == Role.CUSTOMER) {
                // Fetch actual customer accounts
                List<Account> accounts = accountService.getForCustomer(user.getId());
                model.addAttribute("accounts", accounts);

                // Fetch ATM cards for accounts
                List<com.lankatrust.smartbank.entity.AtmCard> atmCards = accounts.stream()
                        .map(acc -> atmCardService.findByAccountId(acc.getId()).orElse(null))
                        .filter(card -> card != null)
                        .collect(Collectors.toList());
                model.addAttribute("atmCards", atmCards);

                // Fetch actual recent transactions
                List<Transaction> transactions = new ArrayList<>();
                for (Account acc : accounts) {
                    transactions.addAll(transactionService.historyForAccount(acc.getId()));
                }
                List<Transaction> recentTxns = transactions.stream()
                        .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                        .limit(5)
                        .collect(Collectors.toList());
                model.addAttribute("recentTransactions", recentTxns);

                // Fetch actual customer loans
                List<Loan> loans = loanService.getForCustomer(user.getId());
                model.addAttribute("loans", loans);
            } else {
                // Staff / Admin statistics
                List<User> customers = userRepository.findByRole(Role.CUSTOMER);
                model.addAttribute("totalCustomers", customers.size());

                List<Account> allAccounts = accountService.getAll();
                model.addAttribute("totalAccounts", allAccounts.size());

                BigDecimal totalBalance = allAccounts.stream()
                        .map(Account::getBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("totalBalance", totalBalance);

                List<Transaction> allTxns = transactionRepository.findAll();
                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                long todayTxnsCount = allTxns.stream()
                        .filter(t -> t.getCreatedAt().isAfter(startOfDay))
                        .count();
                model.addAttribute("todayTransactions", todayTxnsCount);

                BigDecimal totalDeposits = allTxns.stream()
                        .filter(t -> t.getType() == TransactionType.DEPOSIT && t.getStatus() == TransactionStatus.COMPLETED)
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("totalDeposits", totalDeposits);

                BigDecimal totalWithdrawals = allTxns.stream()
                        .filter(t -> t.getType() == TransactionType.WITHDRAWAL && t.getStatus() == TransactionStatus.COMPLETED)
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("totalWithdrawals", totalWithdrawals);

                List<Loan> allLoans = loanRepository.findAll();
                long pendingLoansCount = allLoans.stream()
                        .filter(l -> l.getStatus() != LoanStatus.APPROVED && l.getStatus() != LoanStatus.REJECTED)
                        .count();
                model.addAttribute("pendingLoans", pendingLoansCount);
                model.addAttribute("allLoans", allLoans);

                long activeEmployees = userRepository.findAll().stream()
                        .filter(u -> u.getRole() != Role.CUSTOMER)
                        .count();
                model.addAttribute("activeEmployees", activeEmployees);

                // For list view on staff dashboard
                List<Account> pendingAccounts = allAccounts.stream()
                        .filter(a -> a.getStatus() == AccountStatus.PENDING_APPROVAL)
                        .collect(Collectors.toList());
                model.addAttribute("pendingAccounts", pendingAccounts);
                model.addAttribute("pendingCount", pendingAccounts.size());

                List<Transaction> recentTxns = allTxns.stream()
                        .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                        .limit(5)
                        .collect(Collectors.toList());
                model.addAttribute("recentTransactions", recentTxns);
                model.addAttribute("accounts", allAccounts);

                // ---- Chart.js analytics data (Section 9: Charts & Analytics) ----
                buildAnalyticsAttributes(model, allTxns, allLoans, allAccounts);
            }
        }
        return "bankdash-dashboard";
    }

    /**
     * Builds the data series consumed by Chart.js on the staff/manager/admin
     * dashboard (Section 9: Charts & Analytics of the SmartBank Connect spec).
     * Series are pre-rendered as JS array literals so the JSP can drop them
     * straight into a &lt;script&gt; block with no extra JSON dependency.
     */
    private void buildAnalyticsAttributes(Model model, List<Transaction> allTxns, List<Loan> allLoans, List<Account> allAccounts) {
        // ---- Daily transaction volume: last 7 days ----
        List<String> dayLabels = new ArrayList<>();
        List<Long> dayCounts = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd MMM");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long count = allTxns.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(day))
                    .count();
            dayLabels.add(day.format(dayFmt));
            dayCounts.add(count);
        }
        model.addAttribute("dailyTxnLabels", toJsStringArray(dayLabels));
        model.addAttribute("dailyTxnData", toJsNumberArray(dayCounts));

        // ---- Monthly deposits vs withdrawals: last 6 months ----
        List<String> monthLabels = new ArrayList<>();
        List<BigDecimal> monthlyDeposits = new ArrayList<>();
        List<BigDecimal> monthlyWithdrawals = new ArrayList<>();
        List<Long> monthlyAccountOpenings = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            monthLabels.add(ym.format(monthFmt));

            BigDecimal deposits = allTxns.stream()
                    .filter(t -> t.getType() == TransactionType.DEPOSIT && t.getStatus() == TransactionStatus.COMPLETED
                            && t.getCreatedAt() != null && YearMonth.from(t.getCreatedAt()).equals(ym))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyDeposits.add(deposits);

            BigDecimal withdrawals = allTxns.stream()
                    .filter(t -> t.getType() == TransactionType.WITHDRAWAL && t.getStatus() == TransactionStatus.COMPLETED
                            && t.getCreatedAt() != null && YearMonth.from(t.getCreatedAt()).equals(ym))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyWithdrawals.add(withdrawals);

            long opened = allAccounts.stream()
                    .filter(a -> a.getCreatedAt() != null && YearMonth.from(a.getCreatedAt()).equals(ym))
                    .count();
            monthlyAccountOpenings.add(opened);
        }
        model.addAttribute("monthLabels", toJsStringArray(monthLabels));
        model.addAttribute("monthlyDepositData", toJsDecimalArray(monthlyDeposits));
        model.addAttribute("monthlyWithdrawalData", toJsDecimalArray(monthlyWithdrawals));
        model.addAttribute("accountOpeningData", toJsNumberArray(monthlyAccountOpenings));

        List<BigDecimal> weeklyTransfers = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            BigDecimal total = allTxns.stream()
                    .filter(t -> t.getType() == TransactionType.TRANSFER && t.getStatus() == TransactionStatus.COMPLETED
                            && t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().isEqual(day))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            weeklyTransfers.add(total);
        }
        model.addAttribute("weeklyTransferData", toJsDecimalArray(weeklyTransfers));

        // ---- Loan approval ratio (pie) ----
        long approved = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.APPROVED).count();
        long rejected = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.REJECTED).count();
        long pending = allLoans.size() - approved - rejected;
        model.addAttribute("loanApprovedCount", approved);
        model.addAttribute("loanRejectedCount", rejected);
        model.addAttribute("loanPendingCount", pending);

        // Account opening trend doubles as the customer growth trend series
        // (every approved account is opened for a registered customer).
        model.addAttribute("customerGrowthLabels", toJsStringArray(monthLabels));
    }

    private String toJsStringArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(values.get(i).replace("\"", "")).append("\"");
        }
        return sb.append("]").toString();
    }

    private String toJsNumberArray(List<Long> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(values.get(i));
        }
        return sb.append("]").toString();
    }

    private String toJsDecimalArray(List<BigDecimal> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(values.get(i).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        }
        return sb.append("]").toString();
    }
}
