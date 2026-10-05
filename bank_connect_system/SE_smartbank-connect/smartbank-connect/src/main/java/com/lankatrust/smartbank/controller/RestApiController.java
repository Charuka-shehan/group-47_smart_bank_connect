package com.lankatrust.smartbank.controller;

import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.TransactionService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestApiController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final TransactionService transactionService;
    private final com.lankatrust.smartbank.service.AccountService accountService;

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "application", "SmartBank Connect");
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Map<String, Object>> unread(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("count", 0));
        }
        var user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(user.getId())));
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<?> recent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        var user = userService.getByEmail(authentication.getName());
        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        if (isCustomer) {
            var accounts = accountService.getForCustomer(user.getId());
            var accountIds = accounts.stream().map(a -> a.getId()).collect(java.util.stream.Collectors.toSet());
            var customerTxns = transactionService.getRecentTransactions(50).stream()
                    .filter(t -> (t.getSourceAccount() != null && accountIds.contains(t.getSourceAccount().getId()))
                            || (t.getDestinationAccount() != null && accountIds.contains(t.getDestinationAccount().getId())))
                    .limit(10)
                    .map(t -> Map.<String, Object>of(
                            "reference", t.getReferenceNumber() != null ? t.getReferenceNumber() : "",
                            "amount", t.getAmount() != null ? t.getAmount() : java.math.BigDecimal.ZERO,
                            "type", t.getType() != null ? t.getType().name() : "",
                            "status", t.getStatus() != null ? t.getStatus().name() : ""
                    )).toList();
            return ResponseEntity.ok(customerTxns);
        }

        return ResponseEntity.ok(transactionService.getRecentTransactions(10).stream()
                .map(t -> Map.<String, Object>of(
                        "reference", t.getReferenceNumber() != null ? t.getReferenceNumber() : "",
                        "amount", t.getAmount() != null ? t.getAmount() : java.math.BigDecimal.ZERO,
                        "type", t.getType() != null ? t.getType().name() : "",
                        "status", t.getStatus() != null ? t.getStatus().name() : ""
                )).toList());
    }
}
