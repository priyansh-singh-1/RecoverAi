package com.priyanshu.RecoverAi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "webhook_event_logs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "eventId")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebhookEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private Boolean processed;
}
