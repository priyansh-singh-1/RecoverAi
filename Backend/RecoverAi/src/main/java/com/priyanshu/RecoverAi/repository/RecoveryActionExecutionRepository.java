package com.priyanshu.RecoverAi.repository;

import com.priyanshu.RecoverAi.entity.RecoveryActionExecution;
import com.priyanshu.RecoverAi.enums.RecoveryExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecoveryActionExecutionRepository extends JpaRepository<RecoveryActionExecution,Long> {

    List<RecoveryActionExecution>
    findByRecoveryCaseIdOrderByCreatedAtAsc(Long recoveryCaseId);


    boolean existsByRecoveryCaseIdAndStatusIn(
            Long recoveryCaseId,
            List<RecoveryExecutionStatus> statuses
    );

    long countByStatus(
            RecoveryExecutionStatus status
    );

}
