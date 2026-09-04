package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.entity.Payment;
import com.priyanshu.RecoverAi.enums.RecoveryAction;
import com.priyanshu.RecoverAi.enums.RecoveryPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RecoveryDecisionService {

    public RecoveryPriority determinePriority(Payment payment){
        BigDecimal amount = payment.getAmount();

        if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            return RecoveryPriority.CRITICAL;
        }

        if (amount.compareTo(new BigDecimal("5000")) >= 0) {
            return RecoveryPriority.HIGH;
        }

        if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            return RecoveryPriority.MEDIUM;
        }

        return RecoveryPriority.LOW;
    }

    public RecoveryAction determineInitialAction(Payment payment) {

        if (payment.getAttemptCount() >= 3) {
            return RecoveryAction.STOP;
        }

        String failureReason = payment.getFailureReason();

        if (failureReason == null) {
            return RecoveryAction.ESCALATE_TO_HUMAN;
        }

        return switch (failureReason.toUpperCase()) {

            case "TIMEOUT", "NETWORK_ERROR" ->
                    RecoveryAction.WAIT_AND_RETRY;

            case "INSUFFICIENT_FUNDS" ->
                    RecoveryAction.SEND_REMINDER;

            case "DECLINED" ->
                    RecoveryAction.OFFER_ALTERNATIVE_PAYMENT_METHOD;

            default ->
                    RecoveryAction.ESCALATE_TO_HUMAN;
        };
    }
}
