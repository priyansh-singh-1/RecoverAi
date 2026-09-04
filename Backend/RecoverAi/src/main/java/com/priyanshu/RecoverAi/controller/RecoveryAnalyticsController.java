package com.priyanshu.RecoverAi.controller;

import com.priyanshu.RecoverAi.DTO.RecoveryMetricsResponse;
import com.priyanshu.RecoverAi.service.RecoveryAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class RecoveryAnalyticsController {

    private final RecoveryAnalyticsService recoveryAnalyticsService;

    @GetMapping("/recovery")
    public ResponseEntity<RecoveryMetricsResponse> getRecoveryMetrics(){

        return ResponseEntity.ok(
                recoveryAnalyticsService.getRecoveryMetrics()
        );
    }
}
