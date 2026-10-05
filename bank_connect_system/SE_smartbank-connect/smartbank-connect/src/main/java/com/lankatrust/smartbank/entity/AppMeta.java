package com.lankatrust.smartbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_meta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppMeta {

    @Id
    @Column(name = "meta_key", length = 100)
    private String metaKey;

    @Column(name = "meta_value", length = 400)
    private String metaValue;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
