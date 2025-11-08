package com.order.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AppConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootStrapServer;

	@Bean
	public ConsumerFactory<String, Object> consumerFactory(ObjectMapper objectMapper) {
	    Map<String, Class<?>> topicToClassMap = new HashMap<>();
	    //topicToClassMap.put("order-created", Order.class);//this is topic name "order-created" and object Order.class passed to that topic
	   
	    // Create dynamic deserializer
	    DynamicJsonDeserializer dynamicJsonDeserializer = new DynamicJsonDeserializer(topicToClassMap, objectMapper);

	    // Consumer configuration
	    Map<String, Object> props = new HashMap<>();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);

	    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
	            new ErrorHandlingDeserializer<>(dynamicJsonDeserializer));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
	        ConsumerFactory<String, Object> consumerFactory) {
	    ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(consumerFactory);
	    return factory;
	}


}
