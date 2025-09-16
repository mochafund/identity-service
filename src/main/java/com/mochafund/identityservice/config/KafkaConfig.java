package com.mochafund.identityservice.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaConfig {

    private List<String> topics;

    @Bean
    public List<NewTopic> kafkaTopics() {
        return topics.stream()
                .map(topicName -> TopicBuilder.name(topicName)
                        .partitions(3)
                        .replicas(1)
                        .build())
                .toList();
    }
}