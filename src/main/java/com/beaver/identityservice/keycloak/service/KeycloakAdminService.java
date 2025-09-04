package com.beaver.identityservice.keycloak.service;

import com.beaver.identityservice.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService implements IKeycloakAdminService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Override
    public void syncAttributes(String sub, User user) {
        Map<String, String> set = new HashMap<>();

        if (user.getId() != null) {
            set.put("userId", user.getId().toString());
        }

        if (user.getLastWorkspaceId() != null) {
            set.put("workspaceId", user.getLastWorkspaceId().toString());
        }

        upsertAttributes(sub, set);
    }

    private void upsertAttributes(String sub, Map<String, String> attributes) {
        Objects.requireNonNull(sub, "sub must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");

        try {
            var kcUser = keycloak.realm(realm).users().get(sub);
            var rep = kcUser.toRepresentation();

            Map<String, List<String>> attrs =
                    Optional.ofNullable(rep.getAttributes()).orElseGet(HashMap::new);

            boolean changed = false;

            for (Map.Entry<String, String> e : attributes.entrySet()) {
                final String key = e.getKey();
                final String newVal = e.getValue();
                if (newVal == null) continue; // never remove; just skip

                final List<String> current = attrs.get(key);
                if (current != null && current.size() == 1 && Objects.equals(current.getFirst(), newVal)) {
                    continue; // already up to date, skip
                }

                attrs.put(key, List.of(newVal));
                changed = true;
                log.debug("Upsert attribute '{}'='{}' for sub={}", key, newVal, sub);
            }

            if (changed) {
                rep.setAttributes(attrs);
                kcUser.update(rep);
            } else {
                log.debug("No attribute changes for sub={}, skipping update", sub);
            }

        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keycloak user not found: " + sub, e);
        } catch (jakarta.ws.rs.ForbiddenException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission (need realm-management: manage-users/view-users)", e);
        } catch (jakarta.ws.rs.WebApplicationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to update Keycloak user (" + e.getResponse().getStatus() + ")", e);
        }
    }

    @Override
    public void logoutAllSessions(String sub) {
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
    public void deleteUser(String sub) {
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
