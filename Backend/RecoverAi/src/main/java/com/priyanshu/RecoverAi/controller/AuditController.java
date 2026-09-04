package com.priyanshu.RecoverAi.controller;

import com.priyanshu.RecoverAi.DTO.AuditLogResponse;
import com.priyanshu.RecoverAi.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {

        return ResponseEntity.ok(
                auditService.getAllAuditLogs()
        );
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<AuditLogResponse>> getByPaymentId(
            @PathVariable Long paymentId
    ) {

        return ResponseEntity.ok(
                auditService.getByPaymentId(paymentId)
        );
    }

    @GetMapping("/recovery-case/{recoveryCaseId}")
    public ResponseEntity<List<AuditLogResponse>> getByRecoveryCaseId(
            @PathVariable Long recoveryCaseId
    ) {

        return ResponseEntity.ok(
                auditService.getByRecoveryCaseId(recoveryCaseId)
        );
    }
}
