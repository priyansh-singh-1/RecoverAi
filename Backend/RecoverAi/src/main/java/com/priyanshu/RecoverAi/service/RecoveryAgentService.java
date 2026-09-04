package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.RecoveryAgentContext;
import com.priyanshu.RecoverAi.DTO.RecoveryAgentDecision;
import com.priyanshu.RecoverAi.enums.RecoveryAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecoveryAgentService {

    public RecoveryAgentDecision analyze(RecoveryAgentContext context){
        String diagnosis= diagnoseFailure(context);

        List<String> nextSteps= builNextSteps(context);

        String explanation =
                buildExplanation(context);

        String confidenceLevel= determineConfidence(context.getRecoveryProbability());

        RecoveryAction proposedAction= context.getPolicyAction();

        return RecoveryAgentDecision.builder()
                .diagnosis(diagnosis)
                .ruleBasedAction(context.getRuleBasedAction())
                .recoveryProbability(context.getRecoveryProbability())
                .policyAction(context.getPolicyAction())
                .proposedAction(proposedAction)
                .explanation(explanation)
                .nextSteps(nextSteps)
                .approvalRequired(context.isApprovalRequired())
                .confidenceLevel(confidenceLevel)
                .build();
    }

    private String diagnoseFailure(RecoveryAgentContext context){
        String failureReason =
                context.getFailureReason();

        if ("NETWORK_ERROR".equalsIgnoreCase(failureReason)) {

            return "Payment failed due to a transient network issue.";
        }

        if ("TIMEOUT".equalsIgnoreCase(failureReason)) {

            return "Payment processing timed out before completion.";
        }

        if ("INSUFFICIENT_FUNDS".equalsIgnoreCase(failureReason)) {

            return "Customer account did not have sufficient funds.";
        }

        if ("DECLINED".equalsIgnoreCase(failureReason)) {

            return "The payment method was declined by the payment network or issuer.";
        }

        return "Payment failed for an unclassified reason.";
    }

    private String buildExplanation(
            RecoveryAgentContext context
    ) {

        String mlText;

        if (context.getRecoveryProbability() == null) {
            mlText = "ML recovery probability was unavailable";
        } else {
            mlText =
                    "ML estimated recovery probability at "
                            + context.getRecoveryProbability();
        }

        return "Rule engine recommended "
                + context.getRuleBasedAction()
                + ". "
                + mlText
                + ". Policy engine selected "
                + context.getPolicyAction()
                + " as the safe final recovery strategy.";
    }

    private List<String> builNextSteps(RecoveryAgentContext context){
        List<String> steps =
                new ArrayList<>();

        RecoveryAction action =
                context.getPolicyAction();

        switch (action) {

            case RETRY_PAYMENT -> {

                steps.add(
                        "Wait for a short retry window."
                );

                steps.add(
                        "Retry the payment using the existing payment context."
                );

                steps.add(
                        "Monitor Razorpay webhook for capture or failure."
                );
            }

            case SEND_REMINDER -> {

                steps.add(
                        "Notify the customer about the failed payment."
                );

                steps.add(
                        "Wait for customer funding or manual retry."
                );
            }

            case SEND_PAYMENT_LINK -> {

                steps.add(
                        "Generate a fresh payment link."
                );

                steps.add(
                        "Send the payment link to the customer."
                );

                steps.add(
                        "Track payment status through webhook events."
                );
            }

            case OFFER_ALTERNATIVE_PAYMENT_METHOD -> {

                steps.add(
                        "Ask the customer to use another payment method."
                );

                steps.add(
                        "Generate a payment flow supporting an alternative method."
                );
            }

            case ESCALATE_TO_HUMAN -> {

                steps.add(
                        "Place the recovery case in the human review queue."
                );

                steps.add(
                        "Prevent autonomous high-risk recovery actions."
                );
            }

            case STOP -> {

                steps.add(
                        "Stop further automated recovery attempts."
                );

                steps.add(
                        "Keep the case available for audit and analytics."
                );
            }

            default ->
                    steps.add(
                            "Continue recovery according to policy."
                    );
        }

        return steps;
    }

    private String determineConfidence(BigDecimal probability){
        if(probability ==null){
            return "UNKNOWN";
        }
        if (probability.compareTo(
                new BigDecimal("0.80")
        ) >= 0) {
            return "VERY_HIGH";
        }

        if (probability.compareTo(
                new BigDecimal("0.65")
        ) >= 0) {
            return "HIGH";
        }

        if (probability.compareTo(
                new BigDecimal("0.40")
        ) >= 0) {
            return "MEDIUM";
        }

        return "LOW";

    }
}
