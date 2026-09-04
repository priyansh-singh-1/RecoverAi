package com.priyanshu.RecoverAi.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.priyanshu.RecoverAi.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;

    private String razorpayPaymentId;

    private BigDecimal amount;

    private PaymentStatus status;

    private String failureReason;

    private Integer attemptCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}