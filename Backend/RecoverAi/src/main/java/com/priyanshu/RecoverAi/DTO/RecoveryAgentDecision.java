package com.priyanshu.RecoverAi.DTO;

import com.priyanshu.RecoverAi.enums.RecoveryAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecoveryAgentDecision {

    private String diagnosis;

    private RecoveryAction ruleBasedAction;

    private BigDecimal recoveryProbability;



    private RecoveryAction policyAction;

    private RecoveryAction proposedAction;

    private String explanation;

    private List<String> nextSteps;

    private boolean approvalRequired;

    private String confidenceLevel;
}
