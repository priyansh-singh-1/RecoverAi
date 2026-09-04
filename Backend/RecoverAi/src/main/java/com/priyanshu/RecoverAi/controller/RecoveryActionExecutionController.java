package com.priyanshu.RecoverAi.controller;

import com.priyanshu.RecoverAi.DTO.RecoveryActionExecutionResponse;
import com.priyanshu.RecoverAi.service.RecoveryActionExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recovery-actions")
@RequiredArgsConstructor
public class RecoveryActionExecutionController {

    private final RecoveryActionExecutionService
            recoveryActionExecutionService;

    @PostMapping(
            "/recovery-case/{recoveryCaseId}/execute"
    )
    public ResponseEntity<RecoveryActionExecutionResponse>
    execute(
            @PathVariable Long recoveryCaseId
    ) {

        return ResponseEntity.ok(
                recoveryActionExecutionService
                        .execute(recoveryCaseId)
        );
    }

    @GetMapping(
            "/recovery-case/{recoveryCaseId}"
    )
    public ResponseEntity<
            List<RecoveryActionExecutionResponse>>
    getHistory(
            @PathVariable Long recoveryCaseId
    ) {

        return ResponseEntity.ok(
                recoveryActionExecutionService
                        .getByRecoveryCase(
                                recoveryCaseId
                        )
        );
    }
}