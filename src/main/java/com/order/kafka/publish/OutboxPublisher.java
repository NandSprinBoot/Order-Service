package com.order.kafka.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.bean.OutboxEvent;
import com.order.repository.OrderOutboxRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    @Autowired
    private OrderOutboxRepository outboxRepo;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MeterRegistry meter;

    @Value("${spring.kafka.topics.orders}")
    private String ordersTopic;
    @Value("${spring.kafka.topics.ordersDlq}")
    private String ordersDlq;

    private final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 2000)
    public void publish() {
        List<OutboxEvent> newEvents = outboxRepo.findByStatus("NOT_SENT");
        meter.gauge("outbox.not_sent_count", newEvents.size());
        for (OutboxEvent e : newEvents) {
            try {
                System.out.println(e.getPayload());
                kafkaTemplate.send(ordersTopic, e.getAggregateId(), e.getPayload()).get();
                e.setStatus("SENT");
                e.setRetryCount(0);
                outboxRepo.save(e);
                meter.counter("outbox.sent").increment();
            } catch (Exception ex) {
                e.setRetryCount(e.getRetryCount() + 1);
                if (e.getRetryCount() > MAX_RETRIES) {
                    e.setStatus("FAILED");
                    // push to dlq
                    kafkaTemplate.send(ordersDlq, e.getAggregateId(), e.getPayload());
                    meter.counter("outbox.dlq").increment();
                }
                outboxRepo.save(e);
                meter.counter("outbox.failed").increment();
            }
        }
    }
}


