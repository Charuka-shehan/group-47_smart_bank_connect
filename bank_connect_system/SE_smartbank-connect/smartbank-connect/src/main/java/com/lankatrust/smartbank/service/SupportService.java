package com.lankatrust.smartbank.service;

import com.lankatrust.smartbank.entity.CommunicationLog;
import com.lankatrust.smartbank.entity.StatementRequest;
import com.lankatrust.smartbank.entity.SupportTicket;

import java.time.LocalDate;
import java.util.List;

public interface SupportService {
    SupportTicket createTicket(Long customerId, String subject, String message, String createdByEmail);
    SupportTicket updateStatus(Long ticketId, String status, String resolution, String staffEmail);
    List<SupportTicket> getAllTickets();
    List<SupportTicket> getForCustomer(Long customerId);

    CommunicationLog logCommunication(Long customerId, String staffEmail, String channel, String summary);
    List<CommunicationLog> getCommunications();
    List<CommunicationLog> getCommunicationsForCustomer(Long customerId);

    StatementRequest requestStatement(Long customerId, Long accountId, LocalDate from, LocalDate to, String createdByEmail);
    StatementRequest completeStatement(Long requestId, String staffEmail);
    List<StatementRequest> getStatementRequests();
}
