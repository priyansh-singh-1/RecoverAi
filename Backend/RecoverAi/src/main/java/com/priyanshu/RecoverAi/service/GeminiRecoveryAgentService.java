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

@Service
@RequiredArgsConstructor
public class GeminiRecoveryAgentService {

    private final RestClient restClient;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.model}")
    private String model;

    public LlmRecoveryAgentResponse analyze(
            RecoveryAgentContext context
    ) {

        String prompt =
                buildPrompt(context);

        Map<String, Object> requestBody =
                buildRequestBody(prompt);

        String url =
                apiUrl
                        + "/"
                        + model
                        + ":generateContent";

        String rawResponse =
                restClient.post()
                        .uri(url)
                        .header(
                                "x-goog-api-key",
                                apiKey
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

                Analyze the failed payment and explain the safest
                recovery strategy.

                SAFETY RULES:

                1. You may analyze and explain.
                2. You MUST NOT override the policy-approved action.
                3. proposedAction MUST exactly equal policyAction.
                4. Do not invent payment information.
                5. If ML probability is unavailable, clearly state
                   that ML evidence is unavailable.
                6. Keep the explanation concise and business-friendly.
                7. nextSteps must contain practical operational steps.

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
                - what likely caused the failure,
                - why the policy action is appropriate,
                - what should happen operationally next.
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
                        "type", "OBJECT",

                        "properties",
                        Map.of(
                                "diagnosis",
                                Map.of(
                                        "type",
                                        "STRING"
                                ),

                                "proposedAction",
                                Map.of(
                                        "type",
                                        "STRING"
                                ),

                                "explanation",
                                Map.of(
                                        "type",
                                        "STRING"
                                ),

                                "nextSteps",
                                Map.of(
                                        "type",
                                        "ARRAY",
                                        "items",
                                        Map.of(
                                                "type",
                                                "STRING"
                                        )
                                ),

                                "confidenceLevel",
                                Map.of(
                                        "type",
                                        "STRING"
                                )
                        ),

                        "required",
                        List.of(
                                "diagnosis",
                                "proposedAction",
                                "explanation",
                                "nextSteps",
                                "confidenceLevel"
                        )
                );

        return Map.of(
                "contents",
                List.of(
                        Map.of(
                                "role",
                                "user",

                                "parts",
                                List.of(
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                )
                        )
                ),

                "generationConfig",
                Map.of(
                        "responseMimeType",
                        "application/json",

                        "responseSchema",
                        schema,

                        "temperature",
                        0.2
                )
        );
    }

    private LlmRecoveryAgentResponse parseResponse(
            String rawResponse
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(
                            rawResponse
                    );

            JsonNode candidates =
                    root.path("candidates");

            if (!candidates.isArray()
                    || candidates.isEmpty()) {

                throw new IllegalStateException(
                        "Gemini response contains no candidates"
                );
            }

            JsonNode parts =
                    candidates
                            .get(0)
                            .path("content")
                            .path("parts");

            if (!parts.isArray()
                    || parts.isEmpty()) {

                throw new IllegalStateException(
                        "Gemini response contains no content parts"
                );
            }

            String json =
                    parts
                            .get(0)
                            .path("text")
                            .asText();

            if (json == null
                    || json.isBlank()) {

                throw new IllegalStateException(
                        "Gemini returned empty structured output"
                );
            }

            return objectMapper.readValue(
                    json,
                    LlmRecoveryAgentResponse.class
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to parse Gemini agent response",
                    exception
            );
        }
    }
}