package com.priyanshu.RecoverAi.DTO;

import com.priyanshu.RecoverAi.enums.RecoveryAction;
import com.priyanshu.RecoverAi.enums.RecoveryExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecoveryActionExecutionResponse {

    private Long id;
    private Long recoveryCaseId;

    private RecoveryAction action;
    private RecoveryExecutionStatus status;

    private Integer attemptNumber;

    private String message;
    private String externalReference;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
