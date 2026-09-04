package com.priyanshu.RecoverAi.service;

import com.priyanshu.RecoverAi.DTO.PaymentCreateRequest;
import com.priyanshu.RecoverAi.DTO.PaymentResponse;
import com.priyanshu.RecoverAi.entity.Payment;
import com.priyanshu.RecoverAi.enums.AuditActor;
import com.priyanshu.RecoverAi.enums.AuditEventType;
import com.priyanshu.RecoverAi.enums.PaymentStatus;
import com.priyanshu.RecoverAi.exception.DuplicatePaymentException;
import com.priyanshu.RecoverAi.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RecoveryService recoveryService;
    private final AuditService auditService;

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PaymentResponse> getFailedPayments() {
        return paymentRepository.findByStatus(PaymentStatus.FAILED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .attemptCount(payment.getAttemptCount())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public PaymentResponse createPayment(PaymentCreateRequest request){

        if(paymentRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())){
            throw new DuplicatePaymentException("Payment already exists with Razorpay payment ID: "
                    + request.getRazorpayPaymentId());
        }
        Payment payment= Payment.builder()
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .amount(request.getAmount())
                .status(request.getStatus())
                .failureReason(request.getFailureReason())
                .attemptCount(request.getAttemptCount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if(savedPayment.getStatus() == PaymentStatus.FAILED){
            recoveryService.createRecoveryCase(savedPayment);
        }

        System.out.println("Payment created with Razorpay payment ID: " + savedPayment.getRazorpayPaymentId());
        System.out.println("Payment status: " + savedPayment.getStatus());

        return toResponse(savedPayment);
    }


    @Transactional
    public Payment processFailedWebhook(
            String razorpayPaymentId,
            BigDecimal amount,
            String failureReason
    ) {

        Optional<Payment> existingPayment =
                paymentRepository.findByRazorpayPaymentId(
                        razorpayPaymentId
                );

        Payment payment;

        if (existingPayment.isPresent()) {

            payment = existingPayment.get();

            // Important:
            // CAPTURED payment ko old/out-of-order FAILED event se
            // downgrade nahi karna.
            if (payment.getStatus() == PaymentStatus.CAPTURED) {
                return payment;
            }

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(failureReason);

            Integer currentAttempts =
                    payment.getAttemptCount() == null
                            ? 0
                            : payment.getAttemptCount();

            payment.setAttemptCount(currentAttempts + 1);

            payment.setUpdatedAt(LocalDateTime.now());

        } else {

            LocalDateTime now = LocalDateTime.now();

            payment = Payment.builder()
                    .razorpayPaymentId(razorpayPaymentId)
                    .amount(amount)
                    .status(PaymentStatus.FAILED)
                    .failureReason(failureReason)
                    .attemptCount(1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        Payment savedPayment =
                paymentRepository.save(payment);

        auditService.log(
                savedPayment.getId(),
                null,
                AuditEventType.PAYMENT_FAILED,
                AuditActor.RAZORPAY,
                "Payment failed webhook received",
                null,
                PaymentStatus.FAILED.name(),
                "{\"failureReason\":\"" + failureReason + "\"}"
        );

        recoveryService.createRecoveryCase(savedPayment);


        return savedPayment;
    }

    @Transactional
    public Payment processCapturedWebhook(
            String razorpayPaymentId,
            BigDecimal amount
    ) {

        Optional<Payment> existingPayment =
                paymentRepository.findByRazorpayPaymentId(
                        razorpayPaymentId
                );

        Payment payment;

        if (existingPayment.isPresent()) {

            payment = existingPayment.get();

            // Already captured hai
            if (payment.getStatus() == PaymentStatus.CAPTURED) {
                return payment;
            }

            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setFailureReason(null);
            payment.setUpdatedAt(LocalDateTime.now());

        } else {

            LocalDateTime now = LocalDateTime.now();

            payment = Payment.builder()
                    .razorpayPaymentId(razorpayPaymentId)
                    .amount(amount)
                    .status(PaymentStatus.CAPTURED)
                    .failureReason(null)
                    .attemptCount(1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        Payment savedPayment =
                paymentRepository.save(payment);

        auditService.log(
                savedPayment.getId(),
                null,
                AuditEventType.PAYMENT_CAPTURED,
                AuditActor.RAZORPAY,
                "Payment captured webhook received",
                null,
                PaymentStatus.CAPTURED.name(),
                "{\"amount\":" + amount + "}"
        );

        recoveryService.markRecovered(savedPayment);

        return savedPayment;
    }

}
