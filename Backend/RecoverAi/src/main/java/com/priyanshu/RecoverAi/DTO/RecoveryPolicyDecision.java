package com.priyanshu.RecoverAi.DTO;

import com.priyanshu.RecoverAi.enums.RecoveryAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoveryPolicyDecision {

    private RecoveryAction baselineAction;

    private RecoveryAction finalAction;

    private BigDecimal recoveryProbability;

    private String reason;

    private boolean approvalRequired;


}
