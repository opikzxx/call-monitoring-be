package com.callmonitoring.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_monitoring")
@Getter
@Setter
public class CallMonitoring {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "call_id", nullable = false, unique = true, length = 50)
    private String callId;

    @Column(name = "call_timestamp", nullable = false)
    private OffsetDateTime callTimestamp;

    @Column(name = "cs_name", nullable = false, length = 100)
    private String csName;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "sentiment_score", nullable = false)
    private Integer sentimentScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
