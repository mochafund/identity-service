package com.mochafund.identityservice.kafka;

import com.mochafund.identityservice.common.events.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public <T> void send(EventEnvelope<T> event) {
        EventEnvelope<T> eventToSend = enrichEvent(event);
        eventPublisher.publishEvent(eventToSend);
        log.info("Scheduled {} event for post-commit publishing", eventToSend.getType());
    }

    private <T> EventEnvelope<T> enrichEvent(EventEnvelope<T> event) {
        String actor = event.getActor() != null ? event.getActor() : getCurrentActor();
        UUID correlationId = event.getCorrelationId() != null ? event.getCorrelationId() : getCurrentCorrelationId();

        if (java.util.Objects.equals(actor, event.getActor())
                && java.util.Objects.equals(correlationId, event.getCorrelationId())) {
            return event;
        }

        return event.toBuilder()
                .actor(actor)
                .correlationId(correlationId)
                .build();
    }

    private String getCurrentActor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    Jwt jwt = jwtAuth.getToken();
                    String email = jwt.getClaimAsString("preferred_username");
                    if (email != null && !email.isBlank()) {
                        return email;
                    }
                }
                return auth.getName();
            }
        } catch (Exception e) {
            log.debug("Unable to get current user context: {}", e.getMessage());
        }
        return "SYSTEM";
    }

    private UUID getCurrentCorrelationId() {
        try {
            String correlationId = MDC.get("correlationId");
            if (correlationId != null && !correlationId.isBlank()) {
                return UUID.fromString(correlationId);
            }
        } catch (Exception e) {
            log.debug("Unable to get correlation ID from MDC: {}", e.getMessage());
        }
        return UUID.randomUUID();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private void handleEvent(EventEnvelope<?> event) {
        kafkaTemplate.send(event.getType(), event);
        log.info("Published {} event to Kafka after transaction commit", event.getType());
    }
}
