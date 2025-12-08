package com.order.listner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.bean.Order;
import com.order.bean.OutboxEvent;
import com.order.bean.ProcessedEvent;
import com.order.dto.OrderDTO;
import com.order.repository.OrderOutboxRepository;
import com.order.repository.OrderRepository;
import com.order.repository.ProcessedEventRepository;
import com.order.request.EventEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentEventListener {
    @Autowired
    ProcessedEventRepository processedRepo;
    @Autowired
    OrderRepository orderRepo;
    @Autowired
    OrderOutboxRepository outboxRepo;
    @Autowired
    ObjectMapper mapper;

    @KafkaListener(topics = "payments.events", groupId = "order-service")
    public void onPayment(String msg, Acknowledgment ack) throws Exception {
        JsonNode node = mapper.readTree(msg);
// 1️⃣ Idempotency check
        String eventId = node.path("eventId").asText(null);
        if (eventId != null && processedRepo.existsById(eventId)) {
            ack.acknowledge();
            return;
        }
// 2️⃣ Extract payload
        String orderId = node.path("payload").path("orderId").asText();
        String status = node.path("payload").path("status").asText();
// 3️⃣ Update order
        Order order = orderRepo.findById(Long.valueOf(orderId)).orElseThrow();
        if ("SUCCESS".equals(status)) {
            order.setOrdStatus("PAID");
        } else {
            order.setOrdStatus("PAYMENT_FAILED");
        }

        orderRepo.save(order);

        if ("SUCCESS".equals(status)) {
            // ---- Create OrderDTO for the event ----
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrdId(order.getOrdId());
            orderDTO.setCustName(order.getCustName());
            orderDTO.setCustMobile(order.getCustMobile());
            orderDTO.setOrdStatus(order.getOrdStatus());

            // ---- Build event envelope ----
            EventEnvelope<OrderDTO> eventEnvelope = EventEnvelope.<OrderDTO>builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("OrderConfirmed")
                    .eventVersion("v1")
                    .correlationId(orderId)
                    .traceId(UUID.randomUUID().toString())
                    .timestamp(Instant.now().toString())
                    .payload(orderDTO)
                    .build();

            // ---- Save in Outbox ----
            OutboxEvent outbox = new OutboxEvent();
            outbox.setId(Long.valueOf(UUID.randomUUID().toString()));
            outbox.setEventType("OrderConfirmed.v1");
            outbox.setAggregateId(orderId);
            outbox.setPayload(mapper.writeValueAsString(eventEnvelope));
            outbox.setStatus("NOT_SENT");

            outboxRepo.save(outbox);
        }
        if (eventId != null)
            processedRepo.save(new ProcessedEvent(eventId));
        ack.acknowledge();
    }
}

