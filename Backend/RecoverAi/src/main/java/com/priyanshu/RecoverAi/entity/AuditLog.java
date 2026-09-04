package com.priyanshu.RecoverAi.entity;

import com.priyanshu.RecoverAi.enums.AuditActor;
import com.priyanshu.RecoverAi.enums.AuditEventType;
import io.micrometer.core.annotation.TimedSet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "recovery_case_id")
    private Long recoveryCaseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActor actor;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(length = 500)
    private String oldState;

    @Column(length = 500)
    private String newState;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
