package com.priyanshu.RecoverAi.controller;

import com.priyanshu.RecoverAi.DTO.RecoveryApprovalRequest;
import com.priyanshu.RecoverAi.DTO.RecoveryCaseResponse;
import com.priyanshu.RecoverAi.service.RecoveryApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recovery-approvals")
@RequiredArgsConstructor
public class RecoveryApprovalController {

    private final RecoveryApprovalService
            recoveryApprovalService;

    @PostMapping("/{recoveryCaseId}/approve")
    public ResponseEntity<RecoveryCaseResponse> approve(
            @PathVariable Long recoveryCaseId,
            @Valid @RequestBody RecoveryApprovalRequest request
    ) {

        return ResponseEntity.ok(
                recoveryApprovalService.approve(
                        recoveryCaseId,
                        request
                )
        );
    }

    @PostMapping("/{recoveryCaseId}/reject")
    public ResponseEntity<RecoveryCaseResponse> reject(
            @PathVariable Long recoveryCaseId,
            @Valid @RequestBody RecoveryApprovalRequest request
    ) {

        return ResponseEntity.ok(
                recoveryApprovalService.reject(
                        recoveryCaseId,
                        request
                )
        );
    }
}