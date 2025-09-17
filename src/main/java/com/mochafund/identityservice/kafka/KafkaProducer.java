package com.mochafund.identityservice.kafka;

import com.mochafund.identityservice.common.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public void send(BaseEvent event) {
        eventPublisher.publishEvent(event);
        log.info("Scheduled {} event for post-commit publishing", event.getType());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(BaseEvent event) {
        kafkaTemplate.send(event.getType(), event);
        log.info("Published {} event to Kafka after transaction commit", event.getType());
    }
}
