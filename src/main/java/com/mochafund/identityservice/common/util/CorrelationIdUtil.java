package com.mochafund.identityservice.common.util;

import com.mochafund.identityservice.common.events.EventEnvelope;
import org.slf4j.MDC;

public class CorrelationIdUtil {

    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    public static void setFromEvent(EventEnvelope<?> event) {
        String correlationId = event.getCorrelationId() != null ? event.getCorrelationId().toString() : null;
        if (correlationId != null && !correlationId.trim().isEmpty()) {
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        }
    }

    public static void clear() {
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }

    public static void executeWithCorrelationId(EventEnvelope<?> event, Runnable action) {
        setFromEvent(event);
        try {
            action.run();
        } finally {
            clear();
        }
    }
}