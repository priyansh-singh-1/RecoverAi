package com.priyanshu.RecoverAi.DTO;

import com.priyanshu.RecoverAi.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCreateRequest {

    @NotBlank(message = "Razorpay Payment ID is required")
    private  String razorpayPaymentId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;


    private PaymentStatus status;

    private String failureReason;

    @NotNull(message = "Attempt count is required")
    @Positive(message = "Attempt count must be greater than zero")
    private Integer attemptCount;


}
