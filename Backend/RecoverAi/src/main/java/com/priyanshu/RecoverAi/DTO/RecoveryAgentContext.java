package com.priyanshu.RecoverAi.DTO;

import com.priyanshu.RecoverAi.enums.RecoveryAction;
import com.priyanshu.RecoverAi.enums.RecoveryPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecoveryAgentContext {

    private Long paymentId;

    private Long recoveryCaseId;

    private BigDecimal amount;

    private String failureReason;

    private Integer attemptCount;

    private RecoveryPriority priority;

    private RecoveryAction ruleBasedAction;

    private BigDecimal recoveryProbability;

    private RecoveryAction policyAction;

    private boolean approvalRequired;
}
