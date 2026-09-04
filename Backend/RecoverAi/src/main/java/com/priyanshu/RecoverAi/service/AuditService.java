package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.AuditLogResponse;
import com.priyanshu.RecoverAi.entity.AuditLog;
import com.priyanshu.RecoverAi.enums.AuditActor;
import com.priyanshu.RecoverAi.enums.AuditEventType;
import com.priyanshu.RecoverAi.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditLog log(
            Long paymentId,
            Long recoveryCaseId,
            AuditEventType eventType,
            AuditActor actor,
            String reason,
            String oldState,
            String newState,
            String metadata
    ) {

        AuditLog auditLog = AuditLog.builder()
                .paymentId(paymentId)
                .recoveryCaseId(recoveryCaseId)
                .eventType(eventType)
                .actor(actor)
                .reason(reason)
                .oldState(oldState)
                .newState(newState)
                .metadata(metadata)
                .createdAt(LocalDateTime.now())
                .build();

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLogResponse> getAllAuditLogs(){
        return auditLogRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getByPaymentId(Long paymentId){
        return auditLogRepository
                .findByPaymentIdOrderByCreatedAtAsc(paymentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getByRecoveryCaseId(Long recoveryCaseId) {

        return auditLogRepository
                .findByRecoveryCaseIdOrderByCreatedAtAsc(recoveryCaseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog){
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .paymentId(auditLog.getPaymentId())
                .recoveryCaseId(auditLog.getRecoveryCaseId())
                .eventType(auditLog.getEventType())
                .actor(auditLog.getActor())
                .reason(auditLog.getReason())
                .oldState(auditLog.getOldState())
                .newState(auditLog.getNewState())
                .metadata(auditLog.getMetadata())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }



}
