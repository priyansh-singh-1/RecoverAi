package com.priyanshu.RecoverAi.repository;

import com.priyanshu.RecoverAi.entity.WebhookEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventLogRepository extends JpaRepository<WebhookEventLog,Long> {
    boolean existsByEventId(String eventId);
}
