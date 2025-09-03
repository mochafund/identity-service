package com.beaver.identityservice.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
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
    public void upsertUserAttribute(String sub, String key, String value) {
        try {
            UserResource user = keycloak.realm(realm).users().get(sub);
            log.info("Found user in Keycloak by sub: {}", user.toRepresentation());
            UserRepresentation rep = user.toRepresentation();

            Map<String, List<String>> attrs =
                    Optional.ofNullable(rep.getAttributes()).orElseGet(HashMap::new);

            List<String> current = attrs.get(key);
            if (current != null && current.size() == 1 && Objects.equals(current.getFirst(), value)) {
                log.debug("Attribute '{}' already '{}', skipping (sub={})", key, value, sub);
                return; // idempotent
            }

            attrs.put(key, List.of(value));
            rep.setAttributes(attrs);

            user.update(rep);
            log.debug("Upserted attribute '{}'='{}' for user {}", key, value, sub);

        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keycloak user not found: " + sub, e);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission (need realm-management: manage-users/view-users)", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
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
