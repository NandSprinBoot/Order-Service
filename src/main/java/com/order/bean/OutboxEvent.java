package com.order.bean;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name="order_outbox")
@Data
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String aggregateId; // orderId (message key)
    private String eventType; // e.g. OrderCreated.v1
    @Lob
    private String payload;   // full JSON of EventEnvelope
    private String status;    // NEW, SENT, FAILED
    private Instant createdAt = Instant.now();
    private int retryCount = 0;
}

