package com.order.config;

import org.apache.kafka.common.serialization.Deserializer;
//import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.Map;


public class DynamicJsonDeserializer implements Deserializer<Object> {

    private final ObjectMapper objectMapper;
    private final Map<String, Class<?>> topicToClassMap;

    public DynamicJsonDeserializer(Map<String, Class<?>> topicToClassMap, ObjectMapper objectMapper) {
        this.topicToClassMap = topicToClassMap;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        Class<?> targetClass = topicToClassMap.get(topic);

        if (targetClass == null) {
            throw new IllegalArgumentException("No target class found for topic: " + topic);
        }

        try {
            return objectMapper.readValue(data, targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed for topic: " + topic, e);
        }
    }

    @Override
    public void close() {
        // Cleanup resources if necessary
    }
}

