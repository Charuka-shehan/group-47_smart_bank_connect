package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.StaffCreationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffCreationRequestRepository extends JpaRepository<StaffCreationRequest, Long> {
    List<StaffCreationRequest> findAllByOrderByCreatedAtDesc();
}
