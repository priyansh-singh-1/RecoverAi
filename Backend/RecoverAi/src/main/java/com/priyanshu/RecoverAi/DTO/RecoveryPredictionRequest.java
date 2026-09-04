package com.priyanshu.RecoverAi.DTO;

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
public class RecoveryPredictionRequest {

    private BigDecimal amount;
    private Integer attemptCount;
    private String failureReason;
    private RecoveryPriority priority;
    private String recommendedAction;
}
