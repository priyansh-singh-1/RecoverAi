package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.RecoveryPredictionRequest;
import com.priyanshu.RecoverAi.DTO.RecoveryPredictionResponse;
import com.priyanshu.RecoverAi.config.RestClientConfig;
import com.priyanshu.RecoverAi.entity.Payment;
import com.priyanshu.RecoverAi.entity.RecoveryCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class RecoveryPredictionService {
    private final RestClient restClient;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public RecoveryPredictionResponse predict(
            Payment payment,
            RecoveryCase recoveryCase
    ){
        RecoveryPredictionRequest request=
                RecoveryPredictionRequest.builder()
                        .amount(payment.getAmount())
                        .attemptCount(payment.getAttemptCount())
                        .failureReason(payment.getFailureReason())
                        .priority(recoveryCase.getPriority())
                        .recommendedAction(
                                recoveryCase
                                        .getFinalAction()
                                        .name()
                        )
                        .build();
        System.out.println(
                "Calling ML service for payment: "
                        + payment.getRazorpayPaymentId()
        );

        RecoveryPredictionResponse response= restClient
                .post()
                .uri(mlServiceUrl + "/predict")
                .body(request)
                .retrieve()
                .body(RecoveryPredictionResponse.class);

        System.out.println(
                "ML response: " + response
        );

        return response;
    }
}
