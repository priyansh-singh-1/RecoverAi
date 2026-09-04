package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.LlmRecoveryAgentResponse;
import com.priyanshu.RecoverAi.DTO.RecoveryAgentContext;
import com.priyanshu.RecoverAi.DTO.RecoveryAgentDecision;
import com.priyanshu.RecoverAi.enums.RecoveryAction;
import org.springframework.stereotype.Service;

@Service
public class RecoveryAgentGuardService {

    public RecoveryAgentDecision validate(
            RecoveryAgentContext context,
            LlmRecoveryAgentResponse llmResponse
    ) {

        RecoveryAction proposedAction;

        try {

            proposedAction =
                    RecoveryAction.valueOf(
                            llmResponse
                                    .getProposedAction()
                                    .trim()
                                    .toUpperCase()
                    );

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "LLM proposed an invalid recovery action"
            );
        }


        /*
         * CRITICAL SAFETY GUARD:
         *
         * LLM is NOT allowed to override policy.
         */
        if (proposedAction != context.getPolicyAction()) {

            throw new IllegalStateException(
                    "LLM attempted to override policy action. "
                            + "Policy="
                            + context.getPolicyAction()
                            + ", proposed="
                            + proposedAction
            );
        }


        return RecoveryAgentDecision.builder()
                .diagnosis(
                        llmResponse.getDiagnosis()
                )
                .ruleBasedAction(
                        context.getRuleBasedAction()
                )
                .recoveryProbability(
                        context.getRecoveryProbability()
                )
                .policyAction(
                        context.getPolicyAction()
                )
                .proposedAction(
                        proposedAction
                )
                .explanation(
                        llmResponse.getExplanation()
                )
                .nextSteps(
                        llmResponse.getNextSteps()
                )
                .approvalRequired(
                        context.isApprovalRequired()
                )
                .confidenceLevel(
                        llmResponse.getConfidenceLevel()
                )
                .build();
    }
}