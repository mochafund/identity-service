package com.mochafund.identityservice.keycloak.service;

import com.mochafund.identityservice.keycloak.contributor.AttributeContributor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregates user attributes from all registered AttributeContributor beans.
 * Collects contributions from each contributor and merges them into a single
 * attribute map for Keycloak synchronization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAttributeAggregator {

    private final List<AttributeContributor> contributors;

    /**
     * Aggregates attributes for the given user and workspace context from all contributors.
     * Converts natural types to Keycloak's required List<String> format.
     *
     * @param userId the user ID to collect attributes for
     * @param workspaceId optional workspace context
     * @return merged map of all contributed attributes in Keycloak format
     */
    public Map<String, List<String>> aggregateAttributes(UUID userId, Optional<UUID> workspaceId) {
        Map<String, List<String>> aggregated = new HashMap<>();

        for (AttributeContributor contributor : contributors) {
            try {
                Map<String, Object> contribution = contributor.contribute(userId, workspaceId);
                if (contribution != null) {
                    for (Map.Entry<String, Object> entry : contribution.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();

                        if (key != null && value != null) {
                            if (aggregated.containsKey(key)) {
                                log.warn("[KeycloakAttributeAggregator] Attribute key '{}' provided by multiple contributors for userId={}. " +
                                        "Using contribution from {}, ignoring duplicate.",
                                        key, userId, contributor.getClass().getSimpleName());
                            } else {
                                // Convert value to List<String> format expected by Keycloak API
                                List<String> keycloakValue = convertToKeycloakFormat(value);
                                if (!keycloakValue.isEmpty()) {
                                    aggregated.put(key, keycloakValue);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[KeycloakAttributeAggregator] Error collecting attributes from contributor {} for userId={}: {}",
                        contributor.getClass().getSimpleName(), userId, e.getMessage(), e);
                // Continue with other contributors even if one fails
            }
        }

        log.debug("[KeycloakAttributeAggregator] Aggregated {} attributes from {} contributors for userId={}, workspaceId={}",
                aggregated.size(), contributors.size(), userId, workspaceId.orElse(null));

        return aggregated;
    }

    /**
     * Converts contributor values to the List<String> format required by Keycloak Java client.
     */
    private List<String> convertToKeycloakFormat(Object value) {
        if (value instanceof String) {
            return List.of((String) value);
        } else if (value instanceof List<?> list) {
            try {
                return list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .filter(s -> !s.isBlank())
                        .toList();
            } catch (Exception e) {
                log.warn("[KeycloakAttributeAggregator] Failed to convert list value to string list: {}", value, e);
                return List.of();
            }
        } else {
            String stringValue = value.toString();
            return stringValue.isBlank() ? List.of() : List.of(stringValue);
        }
    }
}
