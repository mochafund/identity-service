package com.mochafund.identityservice.kafka;

import com.mochafund.identityservice.common.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(BaseEvent event) {
        kafkaTemplate.send(event.getType(), event);
    }
}
