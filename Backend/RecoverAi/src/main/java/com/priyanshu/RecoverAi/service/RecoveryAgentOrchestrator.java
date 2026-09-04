package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.LlmRecoveryAgentResponse;
import com.priyanshu.RecoverAi.DTO.RecoveryAgentContext;
import com.priyanshu.RecoverAi.DTO.RecoveryAgentDecision;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecoveryAgentOrchestrator {

    private final GeminiRecoveryAgentService
            geminiRecoveryAgentService;

    private final RecoveryAgentGuardService
            recoveryAgentGuardService;

    private final RecoveryAgentService
            recoveryAgentService;

    public RecoveryAgentDecision analyze(
            RecoveryAgentContext context
    ) {

        try {

            System.out.println(
                    ">>> CALLING GEMINI RECOVERY AGENT"
            );

            LlmRecoveryAgentResponse llmResponse =
                    geminiRecoveryAgentService.analyze(
                            context
                    );

            System.out.println(
                    ">>> GEMINI RESPONSE RECEIVED"
            );

            RecoveryAgentDecision decision =
                    recoveryAgentGuardService.validate(
                            context,
                            llmResponse
                    );

            System.out.println(
                    ">>> GEMINI RESPONSE PASSED POLICY GUARD"
            );

            return decision;

        } catch (Exception exception) {

            System.err.println(
                    ">>> GEMINI FAILED - USING DETERMINISTIC FALLBACK"
            );

            System.err.println(
                    ">>> FAILURE TYPE = "
                            + exception
                            .getClass()
                            .getName()
            );

            System.err.println(
                    ">>> FAILURE MESSAGE = "
                            + exception.getMessage()
            );

            return recoveryAgentService.analyze(
                    context
            );
        }
    }
}