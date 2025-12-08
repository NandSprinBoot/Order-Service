package com.order.bean;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name="processed_events")
@Data
public class ProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private String eventId;
    private Instant processedAt = Instant.now();
    public ProcessedEvent() {}
    public ProcessedEvent(String eventId){
        this.eventId=eventId;
    }
}
