package com.priyanshu.RecoverAi.entity;

import com.priyanshu.RecoverAi.enums.RecoveryAction;
import com.priyanshu.RecoverAi.enums.RecoveryPriority;
import com.priyanshu.RecoverAi.enums.RecoveryStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_cases")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoveryCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecoveryStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal revenueAtRisk;

    @Column(precision = 5, scale = 4)
    private BigDecimal recoveryProbability;

    @Enumerated(EnumType.STRING)
    private RecoveryPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_based_action")
    private RecoveryAction ruleBasedAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_action")
    private RecoveryAction finalAction;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal recoveredAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer recoveryAttemptCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

}
