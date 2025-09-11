package com.mochafund.identityservice.keycloak.contributor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for contributing user attributes to Keycloak synchronization.
 * Implementations provide domain-specific attributes that should be synced
 * to Keycloak user records.
 */
public interface AttributeContributor {

    /**
     * Contributes attributes for the given user and optional workspace context.
     *
     * @param userId the user ID to contribute attributes for
     * @param workspaceId optional workspace context for workspace-scoped attributes
     * @return map of attribute keys to their values (values can be String, List<String>, or other types)
     */
    Map<String, Object> contribute(UUID userId, Optional<UUID> workspaceId);
}
