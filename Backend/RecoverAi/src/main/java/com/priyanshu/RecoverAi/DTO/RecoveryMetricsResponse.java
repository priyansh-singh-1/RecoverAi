package com.priyanshu.RecoverAi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoveryMetricsResponse {

    private Long totalRecoveryCases;
    private Long openCases;
    private Long recoveredCases;
    private Long stoppedCases;

    private BigDecimal totalRevenueAtRisk;
    private BigDecimal recoveredRevenue;
    private BigDecimal outstandingRevenueAtRisk;

    private BigDecimal recoveryRate;

    private Long mlScoredCases;
    private Long aiDecisionChangedCases;

    private Long successfulExecutions;
    private Long failedExecutions;

    private Long approvalRequiredCases;

    private Map<String, Long> priorityBreakdown;
//    private Map<String, Long> actionBreakdown;

    private Map<String, Long> failureReasonBreakdown;

    private Map<String, Long> ruleBasedActionBreakdown;

    private Map<String, Long> finalActionBreakdown;
}
