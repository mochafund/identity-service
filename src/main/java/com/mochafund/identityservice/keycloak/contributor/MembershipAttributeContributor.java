package com.mochafund.identityservice.keycloak.contributor;

import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Contributes membership-related attributes to Keycloak user synchronization.
 * Provides user_id, workspace_id, and roles attributes based on the user's
 * current workspace membership.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipAttributeContributor implements AttributeContributor {

    private final IMembershipService membershipService;

    private static final String USER_ID_KEY = "user_id";
    private static final String WORKSPACE_ID_KEY = "workspace_id";
    private static final String ROLES_KEY = "roles";

    @Override
    public Map<String, Object> contribute(UUID userId, Optional<UUID> workspaceId) {
        Map<String, Object> attributes = new HashMap<>();

        if (userId != null) {
            attributes.put(USER_ID_KEY, userId.toString());
        }

        if (workspaceId.isPresent()) {
            UUID wsId = workspaceId.get();
            attributes.put(WORKSPACE_ID_KEY, wsId.toString());

            membershipService.getUserMembershipInWorkspace(userId, wsId)
                    .ifPresent(membership -> {
                        Set<Role> roles = membership.getRoles();
                        if (roles != null && !roles.isEmpty()) {
                            List<String> roleList = roles.stream()
                                    .map(r -> r.name().trim().toUpperCase())
                                    .filter(s -> !s.isBlank())
                                    .distinct()
                                    .sorted()
                                    .toList();
                            attributes.put(ROLES_KEY, roleList);
                        }
                    });
        }

        log.debug("[MembershipAttributeContributor] contributed {} attributes for userId={}, workspaceId={}",
                attributes.size(), userId, workspaceId.orElse(null));

        return attributes;
    }
}
