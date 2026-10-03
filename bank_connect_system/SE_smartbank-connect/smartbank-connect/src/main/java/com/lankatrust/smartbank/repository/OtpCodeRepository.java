package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    List<OtpCode> findByUserIdAndConsumedFalse(Long userId);
}

