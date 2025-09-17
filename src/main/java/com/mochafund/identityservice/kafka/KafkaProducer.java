package com.mochafund.identityservice.kafka;

import com.mochafund.identityservice.common.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(BaseEvent event) {
        kafkaTemplate.send(event.getType(), event);
        log.info("Published {} event", event.getType());
    }
}
