package com.deoksan.share.audit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_log_user", columnList = "user_id"),
    @Index(name = "idx_audit_log_occurred", columnList = "occurred_at"),
    @Index(name = "idx_audit_log_action", columnList = "action")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 20)
    private String result;

    @Column(length = 45)
    private String ip;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Builder
    private AuditEntry(Long userId, String action, String result, String ip, String detail) {
        this.userId = userId;
        this.action = action;
        this.result = result;
        this.ip = ip;
        this.detail = detail;
        this.occurredAt = Instant.now();
    }
}
