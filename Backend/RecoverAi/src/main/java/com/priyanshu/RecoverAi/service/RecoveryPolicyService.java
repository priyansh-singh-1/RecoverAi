package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.RecoveryPolicyDecision;
import com.priyanshu.RecoverAi.entity.Payment;
import com.priyanshu.RecoverAi.entity.RecoveryCase;
import com.priyanshu.RecoverAi.enums.RecoveryAction;
import com.priyanshu.RecoverAi.enums.RecoveryPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RecoveryPolicyService {

    private static final BigDecimal VERY_HIGH_PROBABILITY= new BigDecimal("0.80");
    private static final BigDecimal HIGH_PROBABILITY= new BigDecimal("0.65");
    private static final BigDecimal MEDIUM_PROBABILITY= new BigDecimal("0.40");
    private static final BigDecimal HIGH_VALUE_PAYMENT= new BigDecimal("10000");

    public RecoveryPolicyDecision decide(
            Payment payment,
            RecoveryCase recoveryCase
    ){
        RecoveryAction baselineAction= recoveryCase.getRuleBasedAction();

        BigDecimal probability= recoveryCase.getRecoveryProbability();

        /*
         * Graceful degradation:
         *
         * If ML service is unavailable or fails to provide a probability,
         * we fall back to the deterministic rule-engine recommendation.
         */

        if(probability==null){
            return RecoveryPolicyDecision.builder()
                    .baselineAction(recoveryCase.getRuleBasedAction())
                    .finalAction(recoveryCase.getFinalAction())
                    .recoveryProbability(null)

                    .reason( "ML probability unavailable. "
                            + "Using deterministic rule-engine recommendation.")
                    .approvalRequired(false)
                    .build();
        }

        Integer attempts= payment.getAttemptCount()== null ? 0 : payment.getAttemptCount();

        String failureReason= payment.getFailureReason() == null ? "UNKNOWN" : payment.getFailureReason();

        BigDecimal amount= payment.getAmount();

        /*
         * Safety rule:
         *
         * If the number of recovery attempts has reached the maximum threshold,
         */

        if(attempts>=3){
            return RecoveryPolicyDecision.builder()
                    .baselineAction(baselineAction)
                    .finalAction(RecoveryAction.STOP)
                    .recoveryProbability(probability)
                    .reason("Maximum retry threshold reached. "
                            + "Further automatic recovery attempts are stopped.")
                    .approvalRequired(false)
                    .build();
        }

        /*
         * Very high recovery probability +
         * temporary technical failure.
         */

        if(probability.compareTo(VERY_HIGH_PROBABILITY)>=0 && isTransientFailure(failureReason)){
                    return RecoveryPolicyDecision.builder()
                            .baselineAction(baselineAction)
                            .finalAction(RecoveryAction.RETRY_PAYMENT)
                            .recoveryProbability(probability)
                            .reason("Very high recovery probability with a transient "
                                    + "technical failure. Immediate retry has "
                                    + "high expected recovery value.")
                            .approvalRequired(false)
                            .build();
        }

        /*
         * High probability but insufficient funds.
         *
         * Immediate retry may irritate customer / fail again,
         * so reminder is safer.
         */


        if(probability.compareTo(HIGH_PROBABILITY)>=0 && "INSUFFICIENT_FUNDS".equalsIgnoreCase(failureReason)){
            return RecoveryPolicyDecision.builder()
                    .baselineAction(baselineAction)
                    .finalAction(RecoveryAction.SEND_REMINDER)
                    .recoveryProbability(probability)
                    .reason("Recovery probability is high, but the failure "
                            + "was caused by insufficient funds. "
                            + "Customer reminder is preferred over immediate retry.")
                    .approvalRequired(false)
                    .build();
        }

        if("DECLINED".equalsIgnoreCase(failureReason) && probability.compareTo(MEDIUM_PROBABILITY)>=0){
            return RecoveryPolicyDecision.builder()
                    .baselineAction(baselineAction)
                    .finalAction(RecoveryAction.OFFER_ALTERNATIVE_PAYMENT_METHOD)
                    .recoveryProbability(probability)
                    .reason("Payment was declined but still has meaningful "
                            + "recovery probability. Offering an alternative "
                            + "payment method reduces repeated decline risk.")
                    .approvalRequired(false)
                    .build();
        }

        /*
         * High-value + uncertain recovery:
         * don't automatically perform risky actions.
         */

        if(amount.compareTo(HIGH_VALUE_PAYMENT) >=0 && probability.compareTo(MEDIUM_PROBABILITY)<0){
            return RecoveryPolicyDecision.builder()
                    .baselineAction(baselineAction)
                    .finalAction(RecoveryAction.ESCALATE_TO_HUMAN)
                    .recoveryProbability(probability)
                    .reason("High-value revenue is at risk while predicted "
                            + "recovery probability is low. Human review "
                            + "is required before further action.")
                    .approvalRequired(true)
                    .build();
        }

        /*
         * Medium probability:
         * use less aggressive recovery.
         */

        if(probability.compareTo(MEDIUM_PROBABILITY)>=0){
            return RecoveryPolicyDecision.builder()
                    .baselineAction(baselineAction)
                    .finalAction(RecoveryAction.SEND_PAYMENT_LINK)
                    .recoveryProbability(probability)
                    .reason("Recovery probability is moderate. "
                            + "A payment link provides a low-friction "
                            + "customer-driven recovery path.")
                    .approvalRequired(false)
                    .build();
        }

        /*
         * Low probability:
         * expensive/aggressive retrying may not be worthwhile.
         */

        return RecoveryPolicyDecision.builder()
                .baselineAction(baselineAction)
                .finalAction(RecoveryAction.ESCALATE_TO_HUMAN)
                .recoveryProbability(probability)
                .reason("Predicted recovery probability is low. "
                        + "Automatic retry is avoided and the case "
                        + "is escalated for review.")
                .approvalRequired(true)
                .build();
    }

    private boolean isTransientFailure(String failureReason){
        return "NETWORK_ERROR".equalsIgnoreCase(failureReason)
                || "TIMEOUT".equalsIgnoreCase(failureReason);
    }
}
