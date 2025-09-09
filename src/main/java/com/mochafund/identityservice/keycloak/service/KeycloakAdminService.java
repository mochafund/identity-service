package com.mochafund.identityservice.keycloak.service;

import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService implements IKeycloakAdminService {

    private final Keycloak keycloak;
    private final IMembershipService membershipService;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Override
    public void syncAttributes(String sub, User user) {
        Map<String, List<String>> desired = new HashMap<>();

        if (user.getId() != null) {
            desired.put("user_id", List.of(user.getId().toString()));
        }

        if (user.getLastWorkspaceId() != null) {
            desired.put("workspace_id", List.of(user.getLastWorkspaceId().toString()));

            membershipService.getUserMembershipInWorkspace(user.getId(), user.getLastWorkspaceId())
                    .ifPresent(membership -> {
                        Set<Role> roles = membership.getRoles();
                        if (roles != null && !roles.isEmpty()) {
                            List<String> roleList = roles.stream()
                                    .map(r -> r.name().trim().toUpperCase())
                                    .filter(s -> !s.isBlank())
                                    .distinct()
                                    .sorted()
                                    .toList();
                            desired.put("roles", roleList);
                        }
                    });
        }

        this.upsertAttributes(sub, desired);
    }

    private void upsertAttributes(String sub, Map<String, List<String>> desired) {
        Objects.requireNonNull(sub, "sub must not be null");
        Objects.requireNonNull(desired, "attributes must not be null");

        try {
            var kcUser = keycloak.realm(realm).users().get(sub);
            var rep = kcUser.toRepresentation();

            Map<String, List<String>> attrs =
                    Optional.ofNullable(rep.getAttributes()).orElseGet(HashMap::new);

            boolean changed = false;

            for (Map.Entry<String, List<String>> e : desired.entrySet()) {
                final String key = e.getKey();
                final List<String> newVal = normalize(e.getValue());
                if (newVal.isEmpty()) continue;

                List<String> current = normalize(attrs.get(key));

                if (current.size() == 1 && current.getFirst().contains(",")) {
                    current = normalize(Arrays.stream(current.getFirst().split(","))
                            .map(String::trim)
                            .toList());
                }

                if (!current.equals(newVal)) {
                    attrs.put(key, newVal);
                    changed = true;
                    log.debug("Upsert attribute '{}'='{}' for sub={}", key, newVal, sub);
                }
            }

            if (changed) {
                rep.setAttributes(attrs);
                kcUser.update(rep);
            } else {
                log.debug("No attribute changes for sub={}, skipping update", sub);
            }

        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keycloak user not found: " + sub, e);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission (need realm-management: manage-users/view-users)", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to update Keycloak user (" + e.getResponse().getStatus() + ")", e);
        }
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
        try {
            keycloak.realm(realm).users().get(sub).logout();
            log.debug("Back-channel logout for {}", sub);
        } catch (NotFoundException e) {
            log.debug("User {} not found during logout; treating as success", sub);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission for logout", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to logout user (" + e.getResponse().getStatus() + ")", e);
        }
    }

    @Override
    public void deleteUser(UUID subject) {
        String sub = subject.toString();
        try {
            keycloak.realm(realm).users().delete(sub);
            log.debug("Deleted user {}", sub);
        } catch (NotFoundException e) {
            log.debug("User {} already deleted; treating as success", sub);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission to delete user", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to delete user (" + e.getResponse().getStatus() + ")", e);
        }
    }
}
