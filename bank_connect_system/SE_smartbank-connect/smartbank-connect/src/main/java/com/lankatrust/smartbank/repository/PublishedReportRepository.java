package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.PublishedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublishedReportRepository extends JpaRepository<PublishedReport, Long> {
    List<PublishedReport> findAllByOrderByCreatedAtDesc();
}
