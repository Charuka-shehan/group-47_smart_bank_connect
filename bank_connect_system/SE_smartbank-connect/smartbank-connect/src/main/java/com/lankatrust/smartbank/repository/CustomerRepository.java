package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerId(String customerId);
    Optional<Customer> findByNic(String nic);
    boolean existsByCustomerId(String customerId);
    boolean existsByNic(String nic);
    boolean existsByPhoneNumber(String phoneNumber);
}
