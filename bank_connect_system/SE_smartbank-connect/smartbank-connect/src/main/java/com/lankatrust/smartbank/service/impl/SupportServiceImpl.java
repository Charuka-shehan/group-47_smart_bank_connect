package com.lankatrust.smartbank.service.impl;

import com.lankatrust.smartbank.entity.*;
import com.lankatrust.smartbank.repository.CommunicationLogRepository;
import com.lankatrust.smartbank.repository.StatementRequestRepository;
import com.lankatrust.smartbank.repository.SupportTicketRepository;
import com.lankatrust.smartbank.service.AccountService;
import com.lankatrust.smartbank.service.AuditLogService;
import com.lankatrust.smartbank.service.NotificationService;
import com.lankatrust.smartbank.service.SupportService;
import com.lankatrust.smartbank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository ticketRepository;
    private final CommunicationLogRepository communicationLogRepository;
    private final StatementRequestRepository statementRequestRepository;
    private final UserService userService;
    private final AccountService accountService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public SupportTicket createTicket(Long customerId, String subject, String message, String createdByEmail) {
        User customer = userService.getById(customerId);
        User creator = userService.getByEmail(createdByEmail);
        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber("TKT" + System.currentTimeMillis() + RANDOM.nextInt(1000))
                .customer(customer)
                .subject(subject)
                .message(message)
                .status("OPEN")
                .createdBy(creator)
                .assignedTo(creator.getRole() == Role.CUSTOMER_RELATIONS_EXECUTIVE ? creator : null)
                .createdAt(LocalDateTime.now())
                .build();
        SupportTicket saved = ticketRepository.save(ticket);
        auditLogService.log(createdByEmail, "SUPPORT_TICKET_CREATED", "SupportTicket", saved.getId(), subject);
        notificationService.send(customer, "Support Request Received",
                "Ticket " + saved.getTicketNumber() + " has been logged: " + subject, NotificationChannel.IN_APP);
        return saved;
    }

    @Override
    @Transactional
    public SupportTicket updateStatus(Long ticketId, String status, String resolution, String staffEmail) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        User staff = userService.getByEmail(staffEmail);
        ticket.setStatus(status);
        ticket.setResolution(resolution);
        ticket.setAssignedTo(staff);
        ticket.setUpdatedAt(LocalDateTime.now());
        SupportTicket saved = ticketRepository.save(ticket);
        auditLogService.log(staffEmail, "SUPPORT_TICKET_UPDATED", "SupportTicket", saved.getId(), status);
        notificationService.send(saved.getCustomer(), "Support Ticket Update",
                "Ticket " + saved.getTicketNumber() + " is now " + status, NotificationChannel.IN_APP);
        return saved;
    }

    @Override
    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<SupportTicket> getForCustomer(Long customerId) {
        return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional
    public CommunicationLog logCommunication(Long customerId, String staffEmail, String channel, String summary) {
        User customer = userService.getById(customerId);
        User staff = userService.getByEmail(staffEmail);
        CommunicationLog log = CommunicationLog.builder()
                .customer(customer)
                .staff(staff)
                .channel(channel)
                .summary(summary)
                .createdAt(LocalDateTime.now())
                .build();
        CommunicationLog saved = communicationLogRepository.save(log);
        auditLogService.log(staffEmail, "COMMUNICATION_LOGGED", "CommunicationLog", saved.getId(), summary);
        return saved;
    }

    @Override
    public List<CommunicationLog> getCommunications() {
        return communicationLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<CommunicationLog> getCommunicationsForCustomer(Long customerId) {
        return communicationLogRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional
    public StatementRequest requestStatement(Long customerId, Long accountId, LocalDate from, LocalDate to, String createdByEmail) {
        User customer = userService.getById(customerId);
        Account account = accountService.getById(accountId);
        User creator = userService.getByEmail(createdByEmail);
        StatementRequest request = StatementRequest.builder()
                .customer(customer)
                .account(account)
                .periodFrom(from)
                .periodTo(to)
                .status("PENDING")
                .handledBy(creator.getRole() == Role.CUSTOMER ? null : creator)
                .createdAt(LocalDateTime.now())
                .build();
        StatementRequest saved = statementRequestRepository.save(request);
        auditLogService.log(createdByEmail, "STATEMENT_REQUESTED", "StatementRequest", saved.getId(),
                account.getAccountNumber());
        return saved;
    }

    @Override
    @Transactional
    public StatementRequest completeStatement(Long requestId, String staffEmail) {
        StatementRequest request = statementRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Statement request not found"));
        request.setStatus("COMPLETED");
        request.setHandledBy(userService.getByEmail(staffEmail));
        request.setCompletedAt(LocalDateTime.now());
        StatementRequest saved = statementRequestRepository.save(request);
        notificationService.send(saved.getCustomer(), "Statement Ready",
                "Your statement request for account " + saved.getAccount().getAccountNumber() + " is ready.",
                NotificationChannel.EMAIL);
        return saved;
    }

    @Override
    public List<StatementRequest> getStatementRequests() {
        return statementRequestRepository.findAllByOrderByCreatedAtDesc();
    }
}
