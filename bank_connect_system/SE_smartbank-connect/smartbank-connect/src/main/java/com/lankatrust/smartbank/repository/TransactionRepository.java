package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.Transaction;
import com.lankatrust.smartbank.entity.TransactionStatus;
import com.lankatrust.smartbank.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
            Long sourceAccountId, Long destinationAccountId);

    /** Same in/out scope as the unfiltered history view above, narrowed to a date range. */
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) "
            + "AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountIdAndCreatedAtBetween(
            @Param("accountId") Long accountId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);

    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<Transaction> findBySourceAccountIdAndTypeAndCreatedAtBetween(
            Long sourceAccountId, TransactionType type, LocalDateTime start, LocalDateTime end);
}
