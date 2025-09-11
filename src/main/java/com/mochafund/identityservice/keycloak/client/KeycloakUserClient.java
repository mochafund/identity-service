package com.mochafund.identityservice.keycloak.client;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Wrapper around Keycloak Admin API for user operations.
 * Provides a clean interface for common user operations and handles
 * Keycloak-specific exceptions consistently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakUserClient {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    /**
     * Retrieves a user representation from Keycloak.
     *
     * @param sub the user's subject identifier
     * @return the user representation
     * @throws ResponseStatusException if user not found or access denied
     */
    public UserRepresentation getUser(String sub) {
        try {
            var kcUser = keycloak.realm(realm).users().get(sub);
            return kcUser.toRepresentation();
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keycloak user not found: " + sub, e);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission (need realm-management: view-users)", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to retrieve Keycloak user (" + e.getResponse().getStatus() + ")", e);
        }
    }

    /**
     * Updates a user in Keycloak.
     *
     * @param sub the user's subject identifier
     * @param userRepresentation the updated user representation
     * @throws ResponseStatusException if user not found or access denied
     */
    public void updateUser(String sub, UserRepresentation userRepresentation) {
        try {
            var kcUser = keycloak.realm(realm).users().get(sub);
            kcUser.update(userRepresentation);
            log.debug("Successfully updated Keycloak user: {}", sub);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keycloak user not found: " + sub, e);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission (need realm-management: manage-users)", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to update Keycloak user (" + e.getResponse().getStatus() + ")", e);
        }
    }

    /**
     * Logs out all sessions for a user in Keycloak.
     *
     * @param sub the user's subject identifier
     * @throws ResponseStatusException if user not found or access denied
     */
    public void logout(String sub) {
        try {
            keycloak.realm(realm).users().get(sub).logout();
            log.debug("Successfully logged out all sessions for Keycloak user: {}", sub);
        } catch (NotFoundException e) {
            log.debug("User {} not found during logout; treating as success", sub);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission (need realm-management: manage-users)", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to logout Keycloak user (" + e.getResponse().getStatus() + ")", e);
        }
    }

    /**
     * Deletes a user from Keycloak.
     *
     * @param sub the user's subject identifier
     * @throws ResponseStatusException if user not found or access denied
     */
    public void delete(String sub) {
        try {
            keycloak.realm(realm).users().get(sub).remove();
            log.debug("Successfully deleted Keycloak user: {}", sub);
        } catch (NotFoundException e) {
            log.debug("User {} already deleted; treating as success", sub);
        } catch (ForbiddenException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Service account lacks permission (need realm-management: manage-users)", e);
        } catch (WebApplicationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to delete Keycloak user (" + e.getResponse().getStatus() + ")", e);
        }
    }
}
