package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.RecoveryApprovalRequest;
import com.priyanshu.RecoverAi.DTO.RecoveryCaseResponse;
import com.priyanshu.RecoverAi.entity.RecoveryCase;
import com.priyanshu.RecoverAi.enums.*;
import com.priyanshu.RecoverAi.exception.RecoveryActionNotAllowedException;
import com.priyanshu.RecoverAi.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecoveryApprovalService {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final RecoveryService recoveryService;
    private final AuditService auditService;

    @Transactional
    public RecoveryCaseResponse approve(
            Long recoveryCaseId,
            RecoveryApprovalRequest request
    ) {

        RecoveryCase recoveryCase =
                recoveryCaseRepository.findById(recoveryCaseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Recovery case not found: "
                                                + recoveryCaseId
                                )
                        );

        if (recoveryCase.getStatus()
                != RecoveryStatus.REQUIRES_APPROVAL) {

            throw new RecoveryActionNotAllowedException(
                    "Recovery case is not awaiting approval"
            );
        }

        RecoveryStatus oldStatus =
                recoveryCase.getStatus();

        recoveryCase.setStatus(
                RecoveryStatus.OPEN
        );

        recoveryCase.setUpdatedAt(
                LocalDateTime.now()
        );

        recoveryCaseRepository.save(
                recoveryCase
        );

        auditService.log(
                recoveryCase.getPayment().getId(),
                recoveryCase.getId(),
                AuditEventType.RECOVERY_APPROVED,
                AuditActor.HUMAN,
                buildReason(
                        "Recovery action approved by human reviewer",
                        request.getReason()
                ),
                oldStatus.name(),
                RecoveryStatus.OPEN.name(),
                buildMetadata(
                        request.getReviewedBy()
                )
        );

        return recoveryService.toResponse(
                recoveryCase
        );
    }

    @Transactional
    public RecoveryCaseResponse reject(
            Long recoveryCaseId,
            RecoveryApprovalRequest request
    ) {

        RecoveryCase recoveryCase =
                recoveryCaseRepository.findById(recoveryCaseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Recovery case not found: "
                                                + recoveryCaseId
                                )
                        );

        if (recoveryCase.getStatus()
                != RecoveryStatus.REQUIRES_APPROVAL) {

            throw new RecoveryActionNotAllowedException(
                    "Recovery case is not awaiting approval"
            );
        }

        RecoveryStatus oldStatus =
                recoveryCase.getStatus();

        RecoveryAction oldAction =
                recoveryCase.getFinalAction();

        recoveryCase.setStatus(
                RecoveryStatus.STOPPED
        );

        recoveryCase.setFinalAction(
                RecoveryAction.STOP
        );

        recoveryCase.setResolvedAt(
                LocalDateTime.now()
        );

        recoveryCase.setUpdatedAt(
                LocalDateTime.now()
        );

        recoveryCaseRepository.save(
                recoveryCase
        );

        auditService.log(
                recoveryCase.getPayment().getId(),
                recoveryCase.getId(),
                AuditEventType.RECOVERY_REJECTED,
                AuditActor.HUMAN,
                buildReason(
                        "Recovery action rejected by human reviewer",
                        request.getReason()
                ),
                oldStatus.name(),
                RecoveryStatus.STOPPED.name(),
                "{"
                        + "\"reviewedBy\":\""
                        + request.getReviewedBy()
                        + "\","
                        + "\"rejectedAction\":\""
                        + oldAction
                        + "\""
                        + "}"
        );

        return recoveryService.toResponse(
                recoveryCase
        );
    }

    private String buildReason(
            String baseReason,
            String reviewerReason
    ) {

        if (reviewerReason == null
                || reviewerReason.isBlank()) {
            return baseReason;
        }

        return baseReason
                + ". Reason: "
                + reviewerReason;
    }

    private String buildMetadata(
            String reviewedBy
    ) {

        return "{"
                + "\"reviewedBy\":\""
                + reviewedBy
                + "\""
                + "}";
    }
}