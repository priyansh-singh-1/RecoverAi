package com.priyanshu.RecoverAi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryPredictionResponse {
    private BigDecimal recoveryProbability;
    private String modelVersion;
}
