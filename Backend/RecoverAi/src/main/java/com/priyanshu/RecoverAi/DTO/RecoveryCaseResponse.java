package com.priyanshu.RecoverAi.DTO;

import com.priyanshu.RecoverAi.enums.RecoveryAction;
import com.priyanshu.RecoverAi.enums.RecoveryPriority;
import com.priyanshu.RecoverAi.enums.RecoveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoveryCaseResponse {

    private Long id;

    private Long paymentId;

    private String razorpayPaymentId;

    private BigDecimal paymentAmount;

    private RecoveryStatus status;

    private BigDecimal revenueAtRisk;

    private BigDecimal recoveryProbability;

    private RecoveryPriority priority;

    private RecoveryAction ruleBasedAction;

    private RecoveryAction finalAction;

    private BigDecimal recoveredAmount;

    private Integer recoveryAttemptCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;
}
