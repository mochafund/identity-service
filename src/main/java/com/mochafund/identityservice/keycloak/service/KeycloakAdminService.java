package com.mochafund.identityservice.keycloak.service;

import com.mochafund.identityservice.common.exception.BadRequestException;
import com.mochafund.identityservice.common.exception.UnauthorizedException;
import com.mochafund.identityservice.keycloak.client.KeycloakUserClient;
import com.mochafund.identityservice.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService implements IKeycloakAdminService {

    private final KeycloakUserClient keycloakUserClient;
    private final KeycloakAttributeAggregator attributeAggregator;

    @Override
    public void syncAttributes(String sub, User user) {
        // Collect attributes from all contributors
        Map<String, List<String>> desired = attributeAggregator.aggregateAttributes(
                user.getId(),
                Optional.ofNullable(user.getLastWorkspaceId())
        );

        // Get current user representation from Keycloak
        UserRepresentation rep = keycloakUserClient.getUser(sub);
        boolean changed = false;

        String userEmail = user.getEmail();
        String kcEmail = rep.getEmail();
        if (userEmail != null && !userEmail.isBlank() && !userEmail.equalsIgnoreCase(kcEmail)) {
            log.info("[Keycloak] Updating email for sub={} from '{}' to '{}'", sub, kcEmail, userEmail);
            rep.setEmail(userEmail);
            changed = true;
        }

        String userGivenName = user.getGivenName();
        String kcGivenName = rep.getFirstName();
        if (userGivenName != null && !userGivenName.isBlank() && !userGivenName.equalsIgnoreCase(kcGivenName)) {
            log.info("[Keycloak] Updating givenName for sub={} from '{}' to '{}'", sub, kcGivenName, userGivenName);
            rep.setFirstName(userGivenName);
            changed = true;
        }

        String userFamilyName = user.getFamilyName();
        String kcFamilyName = rep.getLastName();
        if (userFamilyName != null && !userFamilyName.isBlank() && !userFamilyName.equalsIgnoreCase(kcFamilyName)) {
            log.info("[Keycloak] Updating familyName for sub={} from '{}' to '{}'", sub, kcFamilyName, userFamilyName);
            rep.setLastName(userFamilyName);
        }

        // Update attributes if needed
        if (upsertAttributes(sub, rep, desired)) {
            changed = true;
        }

        if (changed) {
            log.info("[Keycloak] Persisting updates for sub={}", sub);
            keycloakUserClient.updateUser(sub, rep);
        } else {
            log.debug("[Keycloak] No changes for sub={}, skipping update", sub);
        }
    }

    /**
     * Updates the attributes on the given user representation. Returns true if any changes were made.
     */
    private boolean upsertAttributes(String sub, UserRepresentation rep, Map<String, List<String>> desired) {
        Objects.requireNonNull(rep, "user representation must not be null");
        Objects.requireNonNull(desired, "attributes must not be null");

        Map<String, List<String>> attrs = Optional.ofNullable(rep.getAttributes()).orElseGet(HashMap::new);
        boolean changed = false;

        for (Map.Entry<String, List<String>> e : desired.entrySet()) {
            final String key = e.getKey();
            final List<String> newVal = normalize(e.getValue());
            if (newVal.isEmpty()) continue;

            List<String> current = normalize(attrs.get(key));

            if (current.size() == 1 && current.get(0).contains(",")) {
                current = normalize(Arrays.stream(current.get(0).split(","))
                        .map(String::trim)
                        .toList());
            }

            if (!current.equals(newVal)) {
                log.info("[Keycloak] Updating attribute '{}' for sub={} from '{}' to '{}'", key, sub, current, newVal);
                attrs.put(key, newVal);
                changed = true;
            }
        }

        if (changed) {
            rep.setAttributes(attrs);
        }
        return changed;
    }

    private static List<String> normalize(List<String> in) {
        if (in == null) return List.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public void logoutAllSessions(UUID subject) {
        String sub = subject.toString();
        keycloakUserClient.logout(sub);
        log.debug("Back-channel logout for {}", sub);
    }

    @Override
    public void deleteUser(UUID subject) {
        String sub = subject.toString();
        keycloakUserClient.delete(sub);
        log.debug("Deleted user {}", sub);
    }

    @Override
    public void syncAttributes(User user) {
        String subject = getCurrentSubject();
        syncAttributes(subject, user);
    }

    @Override
    public void logoutAllSessions() {
        UUID subject = getCurrentSubjectAsUUID();
        logoutAllSessions(subject);
    }

    @Override
    public void deleteUser() {
        UUID subject = getCurrentSubjectAsUUID();
        deleteUser(subject);
    }

    private String getCurrentSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("No JWT token found in security context");
        }

        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new BadRequestException("JWT missing subject claim");
        }

        return subject;
    }

    private UUID getCurrentSubjectAsUUID() {
        String subject = getCurrentSubject();
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid subject format in JWT");
        }
    }
}
