package com.order;

import com.order.kafka.publish.OutboxPublisher;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=localhost:8762"
})
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"order-created"})
@Import(TestKafkaConsumerConfig.class)
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    private Consumer<String, String> consumer;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void setup() {
        consumer = consumerFactory.createConsumer("order-test", "client-test");
        consumer.subscribe(Collections.singletonList("orders.events"));
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void testCreateOrderApiPublishesKafkaEvent() throws Exception {
        String jsonOrder = """
        {
            "custName": "Amit Kumar",
            "custMobile": "9876543210",
            "ordStatus": "CREATED",
            "item": []
        }
        """;

        // Calling API
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andExpect(status().isOk());

        // Manually trigger outbox publishing (simulate scheduler)
        outboxPublisher.publish();

        // Now consume Kafka messages
        ConsumerRecords<String, String> records =
                KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

        assertFalse(records.isEmpty(), "Expected event not found in Kafka topic");
    }

}
