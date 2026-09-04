package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.RecoveryActionExecutionResponse;
import com.priyanshu.RecoverAi.entity.Payment;
import com.priyanshu.RecoverAi.entity.RecoveryActionExecution;
import com.priyanshu.RecoverAi.entity.RecoveryCase;
import com.priyanshu.RecoverAi.enums.*;
import com.priyanshu.RecoverAi.exception.RecoveryActionNotAllowedException;
import com.priyanshu.RecoverAi.repository.RecoveryActionExecutionRepository;
import com.priyanshu.RecoverAi.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecoveryActionExecutionService {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final RecoveryActionExecutionRepository executionRepository;
    private final AuditService auditService;

    @Transactional(noRollbackFor = RecoveryActionNotAllowedException.class)
    public RecoveryActionExecutionResponse execute(Long recoveryCaseId) {

        RecoveryCase recoveryCase =
                recoveryCaseRepository.findById(recoveryCaseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Recovery case not found: "
                                                + recoveryCaseId
                                )
                        );

        // Guard 1- Terminal cases cannot execute any further actions

        if(recoveryCase.getStatus()== RecoveryStatus.RECOVERED
        || recoveryCase.getStatus() == RecoveryStatus.STOPPED
        || recoveryCase.getStatus() == RecoveryStatus.EXPIRED){
            throw new RecoveryActionNotAllowedException(
                    "Recovery action cannot be executed for terminal case status: "
                            + recoveryCase.getStatus()
            );
        }

        // Guard 2. Maximum 3 recovery attempts
        if (recoveryCase.getRecoveryAttemptCount() >= 3) {

            RecoveryStatus oldStatus = recoveryCase.getStatus();

            RecoveryAction oldAction= recoveryCase.getFinalAction();

            recoveryCase.setStatus(RecoveryStatus.STOPPED);

            recoveryCase.setFinalAction(RecoveryAction.STOP);

            recoveryCase.setResolvedAt(LocalDateTime.now());

            recoveryCase.setUpdatedAt(LocalDateTime.now());

            recoveryCaseRepository.save(recoveryCase);

            auditService.log(
                    recoveryCase.getPayment().getId(),
                    recoveryCase.getId(),
                    AuditEventType.RECOVERY_STOPPED,
                    AuditActor.SYSTEM,
                    "Maximum recovery attempts reached. Recovery workflow stopped.",
                    oldStatus.name(),
                    RecoveryStatus.STOPPED.name(),
                    "{"
                            + "\"attemptCount\":"
                            + recoveryCase.getRecoveryAttemptCount()
                            + ","
                            + "\"previousAction\":\""
                            + oldAction
                            + "\""
                            + "}"
            );

            throw new RecoveryActionNotAllowedException(
                    "Maximum recovery attempts reached. Recovery case has been stopped."
            );
        }

        RecoveryAction action =
                recoveryCase.getFinalAction();

        if (action == null) {
            throw new RecoveryActionNotAllowedException(
                    "Recovery case has no final action"
            );
        }

        // Guard 3: don't start another action while one is active
        boolean activeExecutionExists =
                executionRepository.existsByRecoveryCaseIdAndStatusIn(
                        recoveryCaseId,
                        List.of(
                                RecoveryExecutionStatus.PENDING,
                                RecoveryExecutionStatus.EXECUTING
                        )
                );

        if (activeExecutionExists) {
            throw new RecoveryActionNotAllowedException(
                    "An action execution is already active for this recovery case"
            );
        }

        Payment payment =
                recoveryCase.getPayment();

        int nextAttempt =
                recoveryCase.getRecoveryAttemptCount() + 1;

        if (recoveryCase.getStatus()
                == RecoveryStatus.REQUIRES_APPROVAL) {

            RecoveryActionExecution execution =
                    RecoveryActionExecution.builder()
                            .recoveryCase(recoveryCase)
                            .action(action)
                            .status(
                                    RecoveryExecutionStatus
                                            .REQUIRES_APPROVAL
                            )
                            .attemptNumber(nextAttempt)
                            .message(
                                    "Action requires human approval before execution."
                            )
                            .createdAt(LocalDateTime.now())
                            .build();

            execution =
                    executionRepository.save(execution);

            auditService.log(
                    payment.getId(),
                    recoveryCase.getId(),
                    AuditEventType
                            .RECOVERY_ACTION_REQUIRES_APPROVAL,
                    AuditActor.SYSTEM,
                    "Recovery action blocked until human approval",
                    recoveryCase.getStatus().name(),
                    RecoveryStatus.REQUIRES_APPROVAL.name(),
                    buildMetadata(action, nextAttempt)
            );

            return toResponse(execution);
        }

        RecoveryActionExecution execution =
                RecoveryActionExecution.builder()
                        .recoveryCase(recoveryCase)
                        .action(action)
                        .status(
                                RecoveryExecutionStatus.PENDING
                        )
                        .attemptNumber(nextAttempt)
                        .createdAt(LocalDateTime.now())
                        .build();

        execution =
                executionRepository.save(execution);

        try {

            execution.setStatus(
                    RecoveryExecutionStatus.EXECUTING
            );

            execution.setStartedAt(
                    LocalDateTime.now()
            );

            executionRepository.save(execution);

            auditService.log(
                    payment.getId(),
                    recoveryCase.getId(),
                    AuditEventType.RECOVERY_ACTION_STARTED,
                    AuditActor.SYSTEM,
                    "Recovery action execution started",
                    null,
                    action.name(),
                    buildMetadata(action, nextAttempt)
            );

            executeAction(
                    recoveryCase,
                    execution
            );

            execution.setStatus(
                    RecoveryExecutionStatus.SUCCESS
            );

            execution.setCompletedAt(
                    LocalDateTime.now()
            );

            recoveryCase.setRecoveryAttemptCount(
                    nextAttempt
            );

            recoveryCase.setUpdatedAt(
                    LocalDateTime.now()
            );

            recoveryCaseRepository.save(
                    recoveryCase
            );

            executionRepository.save(
                    execution
            );

            auditService.log(
                    payment.getId(),
                    recoveryCase.getId(),
                    AuditEventType
                            .RECOVERY_ACTION_SUCCEEDED,
                    AuditActor.SYSTEM,
                    "Recovery action executed successfully",
                    action.name(),
                    RecoveryExecutionStatus.SUCCESS.name(),
                    buildMetadata(action, nextAttempt)
            );

            return toResponse(execution);

        } catch (Exception exception) {

            execution.setStatus(
                    RecoveryExecutionStatus.FAILED
            );

            execution.setMessage(
                    exception.getMessage()
            );

            execution.setCompletedAt(
                    LocalDateTime.now()
            );

            executionRepository.save(
                    execution
            );

            recoveryCase.setRecoveryAttemptCount(
                    nextAttempt
            );

            recoveryCase.setUpdatedAt(
                    LocalDateTime.now()
            );

            recoveryCaseRepository.save(
                    recoveryCase
            );

            auditService.log(
                    payment.getId(),
                    recoveryCase.getId(),
                    AuditEventType
                            .RECOVERY_ACTION_FAILED,
                    AuditActor.SYSTEM,
                    "Recovery action execution failed: "
                            + exception.getMessage(),
                    action.name(),
                    RecoveryExecutionStatus.FAILED.name(),
                    buildMetadata(action, nextAttempt)
            );

            return toResponse(execution);
        }
    }

    private void executeAction(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        RecoveryAction action =
                recoveryCase.getFinalAction();

        switch (action) {

            case RETRY_PAYMENT ->
                    executeRetry(
                            recoveryCase,
                            execution
                    );

            case SEND_REMINDER ->
                    executeReminder(
                            recoveryCase,
                            execution
                    );

            case SEND_PAYMENT_LINK ->
                    executePaymentLink(
                            recoveryCase,
                            execution
                    );

            case OFFER_ALTERNATIVE_PAYMENT_METHOD ->
                    executeAlternativePayment(
                            recoveryCase,
                            execution
                    );

            case WAIT_AND_RETRY ->
                    executeWaitAndRetry(
                            recoveryCase,
                            execution
                    );

            case ESCALATE_TO_HUMAN ->
                    executeEscalation(
                            recoveryCase,
                            execution
                    );

            case STOP ->
                    executeStop(
                            recoveryCase,
                            execution
                    );

            case OFFER_DISCOUNT ->
                    executeDiscount(
                            recoveryCase,
                            execution
                    );
        }
    }

    private void executeRetry(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Retry request initiated for failed payment."
        );

        execution.setExternalReference(
                "retry_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        recoveryCase.setStatus(
                RecoveryStatus.IN_PROGRESS
        );
    }

    private void executeReminder(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Payment reminder queued for customer."
        );

        execution.setExternalReference(
                "reminder_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        recoveryCase.setStatus(
                RecoveryStatus.IN_PROGRESS
        );
    }

    private void executePaymentLink(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Recovery payment link generated."
        );

        execution.setExternalReference(
                "plink_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        recoveryCase.setStatus(
                RecoveryStatus.IN_PROGRESS
        );
    }

    private void executeAlternativePayment(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Alternative payment method offered."
        );

        execution.setExternalReference(
                "alternative_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        recoveryCase.setStatus(
                RecoveryStatus.IN_PROGRESS
        );
    }

    private void executeWaitAndRetry(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Retry scheduled after recovery wait window."
        );

        execution.setExternalReference(
                "scheduled_retry_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        recoveryCase.setStatus(
                RecoveryStatus.IN_PROGRESS
        );
    }

    private void executeEscalation(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Recovery case escalated to human review."
        );

        execution.setExternalReference(
                "review_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        recoveryCase.setStatus(
                RecoveryStatus.REQUIRES_APPROVAL
        );
    }

    private void executeStop(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Recovery workflow stopped."
        );

        recoveryCase.setStatus(
                RecoveryStatus.STOPPED
        );

        recoveryCase.setResolvedAt(
                LocalDateTime.now()
        );
    }

    private void executeDiscount(
            RecoveryCase recoveryCase,
            RecoveryActionExecution execution
    ) {

        execution.setMessage(
                "Discount recovery offer prepared."
        );

        execution.setExternalReference(
                "discount_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        recoveryCase.setStatus(
                RecoveryStatus.IN_PROGRESS
        );
    }

    @Transactional(readOnly = true)
    public List<RecoveryActionExecutionResponse>
    getByRecoveryCase(Long recoveryCaseId) {

        return executionRepository
                .findByRecoveryCaseIdOrderByCreatedAtAsc(
                        recoveryCaseId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RecoveryActionExecutionResponse toResponse(
            RecoveryActionExecution execution
    ) {

        return RecoveryActionExecutionResponse
                .builder()
                .id(execution.getId())
                .recoveryCaseId(
                        execution
                                .getRecoveryCase()
                                .getId()
                )
                .action(execution.getAction())
                .status(execution.getStatus())
                .attemptNumber(
                        execution.getAttemptNumber()
                )
                .message(execution.getMessage())
                .externalReference(
                        execution.getExternalReference()
                )
                .createdAt(execution.getCreatedAt())
                .startedAt(execution.getStartedAt())
                .completedAt(execution.getCompletedAt())
                .build();
    }

    private String buildMetadata(
            RecoveryAction action,
            Integer attemptNumber
    ) {

        return "{"
                + "\"action\":\""
                + action
                + "\","
                + "\"attemptNumber\":"
                + attemptNumber
                + "}";
    }
}