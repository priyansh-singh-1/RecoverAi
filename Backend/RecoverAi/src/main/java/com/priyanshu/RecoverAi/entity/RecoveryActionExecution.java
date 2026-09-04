package com.priyanshu.RecoverAi.entity;

import com.priyanshu.RecoverAi.enums.RecoveryAction;
import com.priyanshu.RecoverAi.enums.RecoveryExecutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_action_execution")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecoveryActionExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recovery_case_id", nullable = false)
    private RecoveryCase recoveryCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecoveryAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecoveryExecutionStatus status;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
