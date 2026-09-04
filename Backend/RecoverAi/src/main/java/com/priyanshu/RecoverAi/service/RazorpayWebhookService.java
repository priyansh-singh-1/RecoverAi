package com.priyanshu.RecoverAi.service;


import com.priyanshu.RecoverAi.entity.WebhookEventLog;
import com.priyanshu.RecoverAi.repository.WebhookEventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RazorpayWebhookService {

    private final ObjectMapper objectMapper;

    private final WebhookEventLogRepository webhookEventLogRepository;

    private final PaymentService paymentService;

    public void processWebhook(String eventId, String rawPayload) throws Exception{
        if (webhookEventLogRepository.existsByEventId(eventId)) {
            return;
        }

        JsonNode root= objectMapper.readTree(rawPayload);

        String eventType= root
                .path("event")
                .asText();

        WebhookEventLog eventLog=
                WebhookEventLog.builder()
                        .eventId(eventId)
                        .eventType(eventType)
                        .receivedAt(LocalDateTime.now())
                        .processed(false)
                        .build();

        eventLog= webhookEventLogRepository.save(eventLog);

        switch (eventType){
            case "payment.failed" ->
                    handlePaymentFailed(root);

            case "payment.captured" ->
                    handlePaymentCaptured(root);

            default -> {
                // Ignore unsupported events for now.
            }

        }

        eventLog.setProcessed(true);

        webhookEventLogRepository.save(eventLog);


    }

    private void handlePaymentFailed(JsonNode root) {

        JsonNode paymentEntity = root
                .path("payload")
                .path("payment")
                .path("entity");

        String paymentId =
                paymentEntity
                        .path("id")
                        .asText();

        long amountInPaise =
                paymentEntity
                        .path("amount")
                        .asLong();

        BigDecimal amountInRupees =
                BigDecimal
                        .valueOf(amountInPaise)
                        .movePointLeft(2);

        String failureReason =
                paymentEntity
                        .path("error_reason")
                        .asText(null);

        paymentService.processFailedWebhook(
                paymentId,
                amountInRupees,
                failureReason
        );
    }

    private void handlePaymentCaptured(JsonNode root) {

        JsonNode paymentEntity = root
                .path("payload")
                .path("payment")
                .path("entity");

        String paymentId =
                paymentEntity
                        .path("id")
                        .asText();

        long amountInPaise =
                paymentEntity
                        .path("amount")
                        .asLong();

        BigDecimal amountInRupees =
                BigDecimal
                        .valueOf(amountInPaise)
                        .movePointLeft(2);

        paymentService.processCapturedWebhook(
                paymentId,
                amountInRupees
        );
    }
}
