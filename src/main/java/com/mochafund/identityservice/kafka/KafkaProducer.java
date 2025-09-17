package com.mochafund.identityservice.kafka;

import com.mochafund.identityservice.common.events.BaseEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public void send(BaseEvent event) {
        BaseEvent eventToSend = event;

        if (event.getActor() == null) {
            String actor = getCurrentActor();
            eventToSend = setActorOnEvent(eventToSend, actor);
        }

        if (eventToSend.getCorrelationId() == null) {
            UUID correlationId = getCurrentCorrelationId();
            eventToSend = setCorrelationIdOnEvent(eventToSend, correlationId);
        }

        eventPublisher.publishEvent(eventToSend);
        log.info("Scheduled {} event for post-commit publishing", eventToSend.getType());
    }

    private BaseEvent setActorOnEvent(BaseEvent event, String actor) {
        try {
            // Get the concrete builder class
            var builderMethod = event.getClass().getMethod("toBuilder");
            builderMethod.setAccessible(true);
            var builder = builderMethod.invoke(event);

            // Set actor on the builder
            var actorMethod = builder.getClass().getMethod("actor", String.class);
            actorMethod.setAccessible(true);
            actorMethod.invoke(builder, actor);

            // Build and return
            var buildMethod = builder.getClass().getMethod("build");
            buildMethod.setAccessible(true);
            return (BaseEvent) buildMethod.invoke(builder);
        } catch (Exception e) {
            log.warn("Failed to set actor on event, using original event: {}", e.getMessage());
            return event;
        }
    }

    private BaseEvent setCorrelationIdOnEvent(BaseEvent event, UUID correlationId) {
        try {
            // Get the concrete builder class
            var builderMethod = event.getClass().getMethod("toBuilder");
            builderMethod.setAccessible(true);
            var builder = builderMethod.invoke(event);

            // Set correlation ID on the builder
            var correlationIdMethod = builder.getClass().getMethod("correlationId", UUID.class);
            correlationIdMethod.setAccessible(true);
            correlationIdMethod.invoke(builder, correlationId);

            // Build and return
            var buildMethod = builder.getClass().getMethod("build");
            buildMethod.setAccessible(true);
            return (BaseEvent) buildMethod.invoke(builder);
        } catch (Exception e) {
            log.warn("Failed to set correlation ID on event, using original event: {}", e.getMessage());
            return event;
        }
    }

    private String getCurrentActor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.debug("Unable to get current user context: {}", e.getMessage());
        }
        return "SYSTEM";
    }

    // TODO: Use the correlationId set on the incoming event, otherwise use the X-Correlation-Id or generate a new one
    private UUID getCurrentCorrelationId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String correlationIdHeader = request.getHeader("X-Correlation-Id");

                if (correlationIdHeader != null && !correlationIdHeader.isBlank()) {
                    return UUID.fromString(correlationIdHeader);
                }
            }
        } catch (Exception e) {
            log.debug("Unable to get correlation ID from request: {}", e.getMessage());
        }
        return UUID.randomUUID();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private void handleEvent(BaseEvent event) {
        kafkaTemplate.send(event.getType(), event);
        log.info("Published {} event to Kafka after transaction commit", event.getType());
    }
}
