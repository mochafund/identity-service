package com.mochafund.identityservice.user.service;

import com.mochafund.identityservice.keycloak.service.IKeycloakAdminService;
import com.mochafund.identityservice.user.dto.UpdateUserDto;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.repository.IUserRepository;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.service.IWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IKeycloakAdminService keycloakAdminService;
    private final IWorkspaceService workspaceService;

    @Transactional(readOnly = true)
    public User getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User updateUser(UUID userId, UpdateUserDto userDto) {
        log.info("Updating user with ID: {}", userId);

        User user = this.getUser(userId);
        user.patchFrom(userDto);
        User updatedUser = userRepository.save(user);
        keycloakAdminService.syncAttributes(updatedUser);

        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            keycloakAdminService.logoutAllSessions();
            keycloakAdminService.deleteUser();
            userRepository.deleteById(userId);

            log.info("Successfully deleted user {}", userId);

        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Transactional
    public User createUser(Jwt jwt) {
        final String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT missing subject (sub)");
        }
        final String email = Optional.ofNullable(jwt.getClaimAsString("email"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT missing email"));
        final String givenName = Optional.ofNullable(jwt.getClaimAsString("given_name")).orElse(email);
        final String familyName = Optional.ofNullable(jwt.getClaimAsString("family_name")).orElse(email);

        log.debug("Bootstrapping User: sub={}, email={}", sub, email);

        boolean created = false;
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            try {
                user = userRepository.save(User.builder()
                        .email(email)
                        .givenName(givenName)
                        .familyName(familyName)
                        .isActive(true)
                        .build());
                created = true;
                log.debug("Created userId={} for email={}", user.getId(), email);
            } catch (DataIntegrityViolationException race) {
                // Another request created it between find and save → fetch existing user
                user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT, "User just created but not found"));
                log.debug("Race on create resolved, using existing userId={}", user.getId());
            }
        }

        if (user.getLastWorkspaceId() == null) {
            try {
                Workspace workspace = workspaceService
                        .createWorkspace(
                                user.getId(),
                                CreateWorkspaceDto.builder()
                                        .name(String.format("%s %s's Workspace", givenName, familyName))
                                        .build()
                        );

                user.setLastWorkspaceId(workspace.getId());
                user = userRepository.save(user);
                log.debug("Updated user {} lastWorkspaceId to: {}", user.getId(), workspace.getId());
            } catch (Exception e) {
                log.error("Failed to create default workspace for user {}: {}", user.getId(), e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create default workspace", e);
            }
        }

        try {
            keycloakAdminService.syncAttributes(sub, user);
            log.debug("Keycloak attributes set for email={}, userId={}", user.getEmail(), user.getId());
            return user;
        } catch (Exception e) {
            log.warn("Failed to update Keycloak for sub={}, createdNewUser={}, err={}", sub, created, e.toString());
            // Trigger transaction rollback of any new insert
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak update failed", e);
        }
    }
}
