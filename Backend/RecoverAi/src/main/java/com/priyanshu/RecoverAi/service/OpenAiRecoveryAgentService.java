package com.priyanshu.RecoverAi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyanshu.RecoverAi.DTO.LlmRecoveryAgentResponse;
import com.priyanshu.RecoverAi.DTO.RecoveryAgentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

//@Service
@RequiredArgsConstructor
public class OpenAiRecoveryAgentService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    public LlmRecoveryAgentResponse analyze(
            RecoveryAgentContext context
    ) {

        String prompt = buildPrompt(context);

        Map<String, Object> requestBody =
                buildRequestBody(prompt);

        String rawResponse =
                restClient.post()
                        .uri(apiUrl)
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

        return parseResponse(rawResponse);
    }

    private String buildPrompt(
            RecoveryAgentContext context
    ) {

        return """
                You are RecoverAI, an AI revenue recovery agent.

                Your job is to analyze a failed payment and explain
                the safest next recovery action.

                IMPORTANT SAFETY RULES:
                1. You may analyze and recommend.
                2. You MUST NOT bypass the policy-approved action.
                3. proposedAction MUST equal policyAction.
                4. Do not invent payment data.
                5. If recovery probability is unavailable, explicitly
                   state that ML evidence is unavailable.
                6. Keep explanations concise and business-friendly.

                PAYMENT CONTEXT:

                paymentId: %s
                recoveryCaseId: %s
                amount: %s
                failureReason: %s
                attemptCount: %s
                priority: %s
                ruleBasedAction: %s
                recoveryProbability: %s
                policyAction: %s
                approvalRequired: %s

                Explain:
                - what likely happened,
                - why the policy action is appropriate,
                - the operational next steps.
                """
                .formatted(
                        context.getPaymentId(),
                        context.getRecoveryCaseId(),
                        context.getAmount(),
                        context.getFailureReason(),
                        context.getAttemptCount(),
                        context.getPriority(),
                        context.getRuleBasedAction(),
                        context.getRecoveryProbability(),
                        context.getPolicyAction(),
                        context.isApprovalRequired()
                );
    }

    private Map<String, Object> buildRequestBody(
            String prompt
    ) {

        Map<String, Object> schema =
                Map.of(
                        "type", "object",
                        "properties",
                        Map.of(
                                "diagnosis",
                                Map.of(
                                        "type",
                                        "string"
                                ),

                                "proposedAction",
                                Map.of(
                                        "type",
                                        "string"
                                ),

                                "explanation",
                                Map.of(
                                        "type",
                                        "string"
                                ),

                                "nextSteps",
                                Map.of(
                                        "type",
                                        "array",
                                        "items",
                                        Map.of(
                                                "type",
                                                "string"
                                        )
                                ),

                                "confidenceLevel",
                                Map.of(
                                        "type",
                                        "string"
                                )
                        ),

                        "required",
                        List.of(
                                "diagnosis",
                                "proposedAction",
                                "explanation",
                                "nextSteps",
                                "confidenceLevel"
                        ),

                        "additionalProperties",
                        false
                );

        Map<String, Object> format =
                Map.of(
                        "type",
                        "json_schema",

                        "name",
                        "recovery_agent_decision",

                        "strict",
                        true,

                        "schema",
                        schema
                );

        return Map.of(
                "model",
                model,

                "instructions",
                """
                You are a safe AI revenue recovery agent.
                Follow the supplied policy decision exactly.
                Return only the requested structured decision.
                """,

                "input",
                prompt,

                "text",
                Map.of(
                        "format",
                        format
                )
        );
    }

    private LlmRecoveryAgentResponse parseResponse(
            String rawResponse
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(rawResponse);

            JsonNode output =
                    root.path("output");

            for (JsonNode item : output) {

                if (!"message".equals(
                        item.path("type").asText()
                )) {
                    continue;
                }

                for (JsonNode content :
                        item.path("content")) {

                    if ("output_text".equals(
                            content.path("type").asText()
                    )) {

                        String json =
                                content.path("text")
                                        .asText();

                        return objectMapper.readValue(
                                json,
                                LlmRecoveryAgentResponse.class
                        );
                    }
                }
            }

            throw new IllegalStateException(
                    "OpenAI response did not contain output_text"
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to parse LLM agent response",
                    exception
            );
        }
    }
}