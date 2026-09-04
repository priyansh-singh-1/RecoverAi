package com.priyanshu.RecoverAi.controller;

import com.priyanshu.RecoverAi.DTO.RecoveryCaseResponse;
import com.priyanshu.RecoverAi.service.RecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recovery-cases")
@RequiredArgsConstructor
public class RecoveryController {

    private final RecoveryService recoveryService;

    @GetMapping("/get")
    public List<RecoveryCaseResponse> getAllRecoveryCases() {
        return recoveryService.getAllRecoveryCases();
    }

    @GetMapping("/open")
    public List<RecoveryCaseResponse> getOpenRecoveryCases() {
        return recoveryService.getOpenRecoveryCases();
    }
}
