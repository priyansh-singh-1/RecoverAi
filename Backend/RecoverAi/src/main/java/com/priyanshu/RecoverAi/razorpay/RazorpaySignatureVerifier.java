package com.priyanshu.RecoverAi.razorpay;

import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RazorpaySignatureVerifier {

    private final String webhookSecret;

    public RazorpaySignatureVerifier(
            @Value("${razorpay.webhook.secret}") String webhookSecret
    ) {
        this.webhookSecret = webhookSecret;
    }

    public boolean verify(String rawPayload, String signature) {

        try {
            return Utils.verifyWebhookSignature(
                    rawPayload,
                    signature,
                    webhookSecret
            );
        } catch (Exception exception) {
            return false;
        }
    }


}
