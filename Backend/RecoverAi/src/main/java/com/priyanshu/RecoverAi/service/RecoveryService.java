package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.*;
import com.priyanshu.RecoverAi.entity.Payment;
import com.priyanshu.RecoverAi.entity.RecoveryCase;
import com.priyanshu.RecoverAi.enums.*;
import com.priyanshu.RecoverAi.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecoveryService {
    private final RecoveryCaseRepository recoveryCaseRepository;

    private final RecoveryDecisionService recoveryDecisionService;

    private final AuditService auditService;

    private final RecoveryPredictionService recoveryPredictionService;

    private final RecoveryPolicyService recoveryPolicyService;



    private final RecoveryAgentOrchestrator recoveryAgentOrchestrator;

    public RecoveryCase createRecoveryCase(Payment payment){
        if(recoveryCaseRepository.existsByPaymentId(payment.getId())){
            return recoveryCaseRepository
                    .findByPaymentId(payment.getId())
                    .orElseThrow();
        }

        RecoveryPriority priority = recoveryDecisionService.determinePriority(payment);

        RecoveryAction action = recoveryDecisionService.determineInitialAction(payment);
        LocalDateTime now = LocalDateTime.now();

        RecoveryCase recoveryCase = RecoveryCase.builder()
                .payment(payment)
                .status(RecoveryStatus.OPEN)
                .revenueAtRisk(payment.getAmount())
                .recoveryProbability(null)
                .priority(priority)
                .ruleBasedAction(action)
                .finalAction(action)
                .recoveredAmount(BigDecimal.ZERO)
                .recoveryAttemptCount(0)
                .createdAt(now)
                .updatedAt(now)
                .resolvedAt(null)
                .build();

        RecoveryCase savedCase = recoveryCaseRepository.save(recoveryCase);

        auditService.log(
                payment.getId(),
                savedCase.getId(),
                AuditEventType.RECOVERY_CASE_CREATED,
                AuditActor.SYSTEM,
                "Recovery case created for failed payment",
                null,
                RecoveryStatus.OPEN.name(),
                null
        );

        auditService.log(
                payment.getId(),
                savedCase.getId(),
                AuditEventType.PRIORITY_ASSIGNED,
                AuditActor.RULE_ENGINE,
                "Priority assigned based on payment amount",
                null,
                savedCase.getPriority().name(),
                "{\"amount\":" + payment.getAmount() + "}"
        );

        auditService.log(
                payment.getId(),
                savedCase.getId(),
                AuditEventType.ACTION_RECOMMENDED,
                AuditActor.RULE_ENGINE,
                "Initial recovery action selected from deterministic rules",
                null,
                savedCase.getRuleBasedAction().name(),
                "{\"failureReason\":\"" + payment.getFailureReason() + "\"}"
        );

// =====================================================
// STEP 1: ML PREDICTION
// Failure here must NOT stop the recovery pipeline.
// =====================================================


        try {


            RecoveryPredictionResponse prediction =
                    recoveryPredictionService.predict(
                            payment,
                            savedCase
                    );

            System.out.println(">>> AFTER PREDICT CALL");

            System.out.println(
                    ">>> PREDICTION = " + prediction
            );

            savedCase.setRecoveryProbability(
                    prediction.getRecoveryProbability()
            );

            savedCase.setUpdatedAt(
                    LocalDateTime.now()
            );

            savedCase =
                    recoveryCaseRepository.save(savedCase);


            auditService.log(
                    payment.getId(),
                    savedCase.getId(),
                    AuditEventType.ML_PREDICTION_GENERATED,
                    AuditActor.SYSTEM,
                    "ML model generated recovery probability",
                    null,
                    prediction.getRecoveryProbability().toString(),
                    "{\"modelVersion\":\""
                            + prediction.getModelVersion()
                            + "\"}"
            );

        }  catch (Exception exception) {

        System.err.println(
                "AI Agent unavailable. Recovery decision remains valid. Cause: "
                        + exception.getClass().getSimpleName()
        );
    }


// =====================================================
// STEP 2: POLICY ENGINE
// Policy must ALWAYS run, even if ML probability is null.
// =====================================================

        RecoveryPolicyDecision policyDecision;


        try {

            policyDecision =
                    recoveryPolicyService.decide(
                            payment,
                            savedCase
                    );

            RecoveryAction previousAction =
                    savedCase.getRuleBasedAction();


            savedCase.setFinalAction(
                    policyDecision.getFinalAction()
            );


            if (policyDecision.isApprovalRequired()) {

                savedCase.setStatus(
                        RecoveryStatus.REQUIRES_APPROVAL
                );
            }


            savedCase.setUpdatedAt(
                    LocalDateTime.now()
            );

            savedCase =
                    recoveryCaseRepository.save(savedCase);


            auditService.log(
                    payment.getId(),
                    savedCase.getId(),
                    AuditEventType.POLICY_DECISION_GENERATED,
                    AuditActor.SYSTEM,
                    policyDecision.getReason(),
                    previousAction.name(),
                    policyDecision.getFinalAction().name(),
                    buildPolicyMetadata(policyDecision)
            );

        } catch (Exception exception) {

            System.err.println(
                    "Policy engine failed. Keeping deterministic rule-based action."
            );

            exception.printStackTrace();


            // Safety fallback:
            // policy fail hua toh deterministic action ko final action rakho.

            savedCase.setFinalAction(
                    savedCase.getRuleBasedAction()
            );

            savedCase.setUpdatedAt(
                    LocalDateTime.now()
            );

            savedCase =
                    recoveryCaseRepository.save(savedCase);


            policyDecision =
                    RecoveryPolicyDecision.builder()
                            .baselineAction(
                                    savedCase.getRuleBasedAction()
                            )
                            .finalAction(
                                    savedCase.getRuleBasedAction()
                            )
                            .recoveryProbability(
                                    savedCase.getRecoveryProbability()
                            )
                            .reason(
                                    "Policy engine unavailable. Deterministic rule-based action preserved as safe fallback."
                            )
                            .approvalRequired(false)
                            .build();
        }


// =====================================================
// STEP 3: AI AGENT
// Agent failure must NOT affect the recovery decision.
// =====================================================

        try {

            RecoveryAgentContext agentContext =
                    RecoveryAgentContext.builder()
                            .paymentId(payment.getId())
                            .recoveryCaseId(savedCase.getId())
                            .amount(payment.getAmount())
                            .failureReason(payment.getFailureReason())
                            .attemptCount(payment.getAttemptCount())
                            .priority(savedCase.getPriority())
                            .ruleBasedAction(
                                    savedCase.getRuleBasedAction()
                            )
                            .recoveryProbability(
                                    savedCase.getRecoveryProbability()
                            )
                            .policyAction(
                                    savedCase.getFinalAction()
                            )
                            .approvalRequired(
                                    savedCase.getStatus()
                                            == RecoveryStatus.REQUIRES_APPROVAL
                            )
                            .build();


            RecoveryAgentDecision agentDecision =
                    recoveryAgentOrchestrator.analyze(
                            agentContext
                    );


            auditService.log(
                    payment.getId(),
                    savedCase.getId(),
                    AuditEventType.AI_AGENT_ANALYSIS_GENERATED,
                    AuditActor.AI_AGENT,
                    agentDecision.getExplanation(),
                    savedCase.getFinalAction() == null
                            ? null
                            : savedCase.getFinalAction().name(),
                    agentDecision.getProposedAction() == null
                            ? null
                            : agentDecision.getProposedAction().name(),
                    buildAgentMetadata(agentDecision)
            );

        } catch (Exception exception) {

            System.err.println(
                    "AI Agent unavailable. Recovery decision remains valid."
            );

            exception.printStackTrace();
        }
        return savedCase;


    }

    @Transactional(readOnly = true)
    public List<RecoveryCaseResponse> getAllRecoveryCases(){
        return recoveryCaseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecoveryCaseResponse> getOpenRecoveryCases(){
        return recoveryCaseRepository.findByStatus(RecoveryStatus.OPEN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RecoveryCaseResponse toResponse(RecoveryCase recoveryCase) {

        Payment payment = recoveryCase.getPayment();

        return RecoveryCaseResponse.builder()
                .id(recoveryCase.getId())
                .paymentId(recoveryCase.getPayment().getId())
                .razorpayPaymentId(
                        recoveryCase.getPayment().getRazorpayPaymentId()
                )
                .paymentAmount(
                        recoveryCase.getPayment().getAmount()
                )
                .status(recoveryCase.getStatus())
                .revenueAtRisk(recoveryCase.getRevenueAtRisk())
                .recoveryProbability(
                        recoveryCase.getRecoveryProbability()
                )
                .priority(recoveryCase.getPriority())
                .ruleBasedAction(
                        recoveryCase.getRuleBasedAction()
                )
                .finalAction(recoveryCase.getFinalAction())
                .recoveredAmount(
                        recoveryCase.getRecoveredAmount()
                )
                .recoveryAttemptCount(
                        recoveryCase.getRecoveryAttemptCount()
                )
                .createdAt(recoveryCase.getCreatedAt())
                .updatedAt(recoveryCase.getUpdatedAt())
                .resolvedAt(recoveryCase.getResolvedAt())
                .build();
    }

    @Transactional
    public void markRecovered(Payment payment) {

        recoveryCaseRepository
                .findByPaymentId(payment.getId())
                .ifPresent(recoveryCase -> {

                    // Already recovered hai toh dobara state transition mat karo
                    if (recoveryCase.getStatus() == RecoveryStatus.RECOVERED) {
                        return;
                    }

                    RecoveryStatus oldStatus =
                            recoveryCase.getStatus();

                    LocalDateTime now =
                            LocalDateTime.now();

                    recoveryCase.setStatus(
                            RecoveryStatus.RECOVERED
                    );

                    recoveryCase.setRecoveredAmount(
                            payment.getAmount()
                    );

                    recoveryCase.setFinalAction(
                            RecoveryAction.STOP
                    );

                    recoveryCase.setResolvedAt(now);
                    recoveryCase.setUpdatedAt(now);

                    RecoveryCase savedCase =
                            recoveryCaseRepository.save(recoveryCase);

                    auditService.log(
                            payment.getId(),
                            savedCase.getId(),
                            AuditEventType.RECOVERY_MARKED_SUCCESS,
                            AuditActor.SYSTEM,
                            "Payment was captured, so recovery case was marked recovered",
                            oldStatus.name(),
                            RecoveryStatus.RECOVERED.name(),
                            "{\"recoveredAmount\":" + payment.getAmount() + "}"
                    );

                    auditService.log(
                            payment.getId(),
                            savedCase.getId(),
                            AuditEventType.RECOVERY_STOPPED,
                            AuditActor.SYSTEM,
                            "Recovery actions stopped because payment was captured",
                            null,
                            RecoveryAction.STOP.name(),
                            null
                    );
                });
    }

    private String buildPolicyMetadata(RecoveryPolicyDecision decision){
        String probability= decision.getRecoveryProbability()==null ? "null" : decision.getRecoveryProbability().toString();

        return "{"
                +   "\"baselineAction\":\""
                + decision.getBaselineAction()
                + "\","
                + "\"recoveryProbability\":"
                + probability
                + ","
                + "\"approvalRequired\":"
                + decision.isApprovalRequired()
                + "}";
    }

    private String buildAgentMetadata(
            RecoveryAgentDecision decision
    ) {

        String probability =
                decision.getRecoveryProbability() == null
                        ? "null"
                        : decision.getRecoveryProbability().toString();

        String nextSteps =
                decision.getNextSteps() == null
                        ? ""
                        : String.join(
                        " | ",
                        decision.getNextSteps()
                );

        return "{"
                + "\"diagnosis\":\""
                + escapeJson(decision.getDiagnosis())
                + "\","
                + "\"ruleBasedAction\":\""
                + decision.getRuleBasedAction()
                + "\","
                + "\"recoveryProbability\":"
                + probability
                + ","
                + "\"policyAction\":\""
                + decision.getPolicyAction()
                + "\","
                + "\"proposedAction\":\""
                + decision.getProposedAction()
                + "\","
                + "\"confidenceLevel\":\""
                + decision.getConfidenceLevel()
                + "\","
                + "\"approvalRequired\":"
                + decision.isApprovalRequired()
                + ","
                + "\"nextSteps\":\""
                + escapeJson(nextSteps)
                + "\""
                + "}";
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
