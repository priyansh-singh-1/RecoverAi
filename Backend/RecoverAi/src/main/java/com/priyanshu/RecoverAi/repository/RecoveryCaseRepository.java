package com.priyanshu.RecoverAi.repository;

import com.priyanshu.RecoverAi.entity.RecoveryCase;
import com.priyanshu.RecoverAi.enums.RecoveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecoveryCaseRepository extends JpaRepository<RecoveryCase,Long> {

    Optional<RecoveryCase> findByPaymentId(Long paymentId);

    boolean existsByPaymentId(Long paymentId);

    List<RecoveryCase> findByStatus(RecoveryStatus status);
}
