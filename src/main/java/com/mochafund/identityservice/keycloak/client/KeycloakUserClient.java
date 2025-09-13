package com.mochafund.identityservice.keycloak.client;

import com.mochafund.identityservice.common.exception.AccessDeniedException;
import com.mochafund.identityservice.common.exception.InternalServerException;
import com.mochafund.identityservice.common.exception.ResourceNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakUserClient {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    public UserRepresentation getUser(String sub) {
        try {
            var kcUser = keycloak.realm(realm).users().get(sub);
            return kcUser.toRepresentation();
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException(String.format("Keycloak user not found: %s", sub));
        } catch (ForbiddenException e) {
            throw new AccessDeniedException("Service account lacks permission (need realm-management: view-users)");
        } catch (WebApplicationException e) {
            throw new InternalServerException(String.format("Failed to retrieve Keycloak User (%s)", e.getResponse().getStatus()));
        }
    }

    public void updateUser(String sub, UserRepresentation userRepresentation) {
        try {
            var kcUser = keycloak.realm(realm).users().get(sub);
            kcUser.update(userRepresentation);
            log.debug("Successfully updated Keycloak user: {}", sub);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException(String.format("Keycloak user not found: %s", sub));
        } catch (ForbiddenException e) {
            throw new AccessDeniedException("Service account lacks permission (need realm-management: manage-users)");
        } catch (WebApplicationException e) {
            throw new InternalServerException(String.format("Failed to update Keycloak User (%s)", e.getResponse().getStatus()));
        }
    }

    public void logout(String sub) {
        try {
            keycloak.realm(realm).users().get(sub).logout();
            log.debug("Successfully logged out all sessions for Keycloak user: {}", sub);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException(String.format("Keycloak user not found: %s", sub));
       } catch (ForbiddenException e) {
            throw new AccessDeniedException("Service account lacks permission (need realm-management: manage-users)");
        } catch (WebApplicationException e) {
            throw new InternalServerException(String.format("Failed to logout Keycloak User (%s)", e.getResponse().getStatus()));
        }
    }

    public void delete(String sub) {
        try {
            keycloak.realm(realm).users().get(sub).remove();
            log.debug("Successfully deleted Keycloak user: {}", sub);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException(String.format("Keycloak user not found: %s", sub));
       } catch (ForbiddenException e) {
            throw new AccessDeniedException("Service account lacks permission (need realm-management: manage-users)");
        } catch (WebApplicationException e) {
            throw new InternalServerException(String.format("Failed to delete Keycloak User (%s)", e.getResponse().getStatus()));
        }
    }
}
