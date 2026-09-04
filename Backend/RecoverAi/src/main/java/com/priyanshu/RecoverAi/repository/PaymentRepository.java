package com.priyanshu.RecoverAi.repository;

import com.priyanshu.RecoverAi.entity.Payment;
import com.priyanshu.RecoverAi.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);


}
