package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.RecoveryMetricsResponse;
import com.priyanshu.RecoverAi.entity.RecoveryCase;
import com.priyanshu.RecoverAi.enums.RecoveryStatus;
import com.priyanshu.RecoverAi.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.priyanshu.RecoverAi.enums.RecoveryExecutionStatus;
import com.priyanshu.RecoverAi.repository.RecoveryActionExecutionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecoveryAnalyticsService {

    private final RecoveryCaseRepository recoveryCaseRepository;

    private final RecoveryActionExecutionRepository recoveryActionExecutionRepository;

    @Transactional(readOnly = true)
    public RecoveryMetricsResponse getRecoveryMetrics() {

        List<RecoveryCase> cases =
                recoveryCaseRepository.findAll();

        long totalRecoveryCases =
                cases.size();

        long openCases =
                cases.stream()
                        .filter(c ->
                                c.getStatus()
                                        == RecoveryStatus.OPEN
                        )
                        .count();

        long recoveredCases =
                cases.stream()
                        .filter(c ->
                                c.getStatus()
                                        == RecoveryStatus.RECOVERED
                        )
                        .count();

        long stoppedCases =
                cases.stream()
                        .filter(c ->
                                c.getStatus()
                                        == RecoveryStatus.STOPPED
                        )
                        .count();

        long approvalRequiredCases =
                cases.stream()
                        .filter(c ->
                                c.getStatus()
                                        == RecoveryStatus.REQUIRES_APPROVAL
                        )
                        .count();

        BigDecimal totalRevenueAtRisk =
                cases.stream()
                        .map(RecoveryCase::getRevenueAtRisk)
                        .filter(Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal recoveredRevenue =
                cases.stream()
                        .map(RecoveryCase::getRecoveredAmount)
                        .filter(Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal outstandingRevenueAtRisk =
                totalRevenueAtRisk.subtract(
                        recoveredRevenue
                );

        BigDecimal recoveryRate =
                BigDecimal.ZERO;

        if (totalRevenueAtRisk.compareTo(
                BigDecimal.ZERO
        ) > 0) {

            recoveryRate =
                    recoveredRevenue
                            .divide(
                                    totalRevenueAtRisk,
                                    4,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(
                                    BigDecimal.valueOf(100)
                            );
        }

        long mlScoredCases =
                cases.stream()
                        .filter(c ->
                                c.getRecoveryProbability()
                                        != null
                        )
                        .count();

        long aiDecisionChangedCases =
                cases.stream()
                        .filter(c ->
                                c.getRuleBasedAction()
                                        != null
                                        &&
                                        c.getFinalAction()
                                                != null
                                        &&
                                        c.getRuleBasedAction()
                                                != c.getFinalAction()
                        )
                        .count();

        Map<String, Long> priorityBreakdown =
                cases.stream()
                        .filter(c ->
                                c.getPriority() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        c -> c.getPriority().name(),
                                        Collectors.counting()
                                )
                        );

        Map<String, Long> ruleBasedActionBreakdown =
                cases.stream()
                        .filter(c ->
                                c.getRuleBasedAction()
                                        != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        c -> c.getRuleBasedAction()
                                                .name(),
                                        Collectors.counting()
                                )
                        );

        Map<String, Long> finalActionBreakdown =
                cases.stream()
                        .filter(c ->
                                c.getFinalAction()
                                        != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        c -> c.getFinalAction()
                                                .name(),
                                        Collectors.counting()
                                )
                        );

        Map<String, Long> failureReasonBreakdown =
                cases.stream()
                        .filter(c ->
                                c.getPayment() != null
                                        &&
                                        c.getPayment()
                                                .getFailureReason()
                                                != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        c -> c.getPayment()
                                                .getFailureReason(),
                                        Collectors.counting()
                                )
                        );

        long successfulExecutions =
                recoveryActionExecutionRepository
                        .countByStatus(
                                RecoveryExecutionStatus.SUCCESS
                        );

        long failedExecutions =
                recoveryActionExecutionRepository
                        .countByStatus(
                                RecoveryExecutionStatus.FAILED
                        );

        return RecoveryMetricsResponse.builder()

                .totalRecoveryCases(
                        totalRecoveryCases
                )

                .openCases(
                        openCases
                )

                .recoveredCases(
                        recoveredCases
                )

                .stoppedCases(
                        stoppedCases
                )

                .approvalRequiredCases(
                        approvalRequiredCases
                )

                .totalRevenueAtRisk(
                        totalRevenueAtRisk
                )

                .recoveredRevenue(
                        recoveredRevenue
                )

                .outstandingRevenueAtRisk(
                        outstandingRevenueAtRisk
                )

                .recoveryRate(
                        recoveryRate
                )

                .mlScoredCases(
                        mlScoredCases
                )

                .aiDecisionChangedCases(
                        aiDecisionChangedCases
                )

                .successfulExecutions(
                        successfulExecutions
                )

                .failedExecutions(
                        failedExecutions
                )

                .priorityBreakdown(
                        priorityBreakdown
                )

                .ruleBasedActionBreakdown(
                        ruleBasedActionBreakdown
                )

                .finalActionBreakdown(
                        finalActionBreakdown
                )

                .failureReasonBreakdown(
                        failureReasonBreakdown
                )

                .build();
    }
    private BigDecimal calculateRecoveryRate(
            BigDecimal totalRevenueAtRisk,
            BigDecimal recoveredRevenue
    ){
        if(totalRevenueAtRisk.compareTo(BigDecimal.ZERO)==0){
            return BigDecimal.ZERO;
        }

        return recoveredRevenue
                .divide(
                        totalRevenueAtRisk,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(new BigDecimal("100"))
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

}
