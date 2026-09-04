package com.priyanshu.RecoverAi.controller;

import com.priyanshu.RecoverAi.razorpay.RazorpaySignatureVerifier;
import com.priyanshu.RecoverAi.service.RazorpayWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/razorpay")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final RazorpaySignatureVerifier signatureVerifier;

    private final RazorpayWebhookService webhookService;

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody String rawPayload,

            @RequestHeader("X-Razorpay-Signature")
            String signature,

            @RequestHeader("x-razorpay-event-id")
            String eventID
    ){
        boolean valid= signatureVerifier.verify(
                rawPayload,
                signature
        );

        if(!valid){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        try {
            webhookService.processWebhook(
                    eventID,
                    rawPayload
            );
            return ResponseEntity.ok().build();


        }catch (Exception exception){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
