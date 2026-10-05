package com.lankatrust.smartbank.repository;

import com.lankatrust.smartbank.entity.AppMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppMetaRepository extends JpaRepository<AppMeta, String> {
}
