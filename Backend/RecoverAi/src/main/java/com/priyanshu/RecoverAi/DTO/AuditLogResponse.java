package com.priyanshu.RecoverAi.DTO;

import com.priyanshu.RecoverAi.enums.AuditActor;
import com.priyanshu.RecoverAi.enums.AuditEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;
    private Long paymentId;
    private Long recoveryCaseId;

    private AuditEventType eventType;
    private AuditActor actor;

    private String reason;
    private String oldState;
    private String newState;
    private String metadata;

    private LocalDateTime createdAt;
}
