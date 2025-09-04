package com.beaver.identityservice.user.service;

import com.beaver.identityservice.keycloak.service.IKeycloakAdminService;
import com.beaver.identityservice.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.repository.IUserRepository;
import com.beaver.identityservice.workspace.service.IWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IKeycloakAdminService keycloakAdminService;
    private final IWorkspaceService workspaceService;

    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) log.debug("Found user with id: {}", user.get().getId());
        else log.debug("No user found with email: {}", email);
        return user;
    }

    public User save(User user) {
        log.debug("Saving user: {}", user.getEmail());
        User savedUser = userRepository.save(user);
        log.debug("Saved user with id: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    public void bootstrap(Jwt jwt) {
        final String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT missing subject (sub)");
        }
        final String email = Optional.ofNullable(jwt.getClaimAsString("email"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT missing email"));
        final String name = Optional.ofNullable(jwt.getClaimAsString("name")).orElse(email);

        log.debug("Bootstrapping User: sub={}, email={}", sub, email);

        boolean created = false;
        User user = this.findByEmail(email).orElse(null);

        if (user == null) {
            try {
                user = this.save(User.builder()
                        .email(email)
                        .name(name)
                        .isActive(true)
                        .build());
                created = true;
                log.debug("Created userId={} for email={}", user.getId(), email);
            } catch (DataIntegrityViolationException race) {
                // Another request created it between find and save → fetch existing user
                user = this.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT, "User just created but not found"));
                log.debug("Race on create resolved, using existing userId={}", user.getId());
            }
        }

        if (user.getLastWorkspaceId() == null) {
            try {
                WorkspaceMembership membership = workspaceService.createDefaultWorkspace(user);
                log.debug("Created default workspace for {} with membership: {}", user.getEmail(), membership.getId());

                user.setLastWorkspaceId(membership.getWorkspace().getId());
                user = this.save(user);
                log.debug("Updated user {} lastWorkspaceId to: {}", user.getId(), membership.getWorkspace().getId());
            } catch (Exception e) {
                log.error("Failed to create default workspace for user {}: {}", user.getId(), e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create default workspace", e);
            }
        }

        try {
            keycloakAdminService.syncAttributes(sub, user);
            log.debug("Keycloak attributes set for email={}, userId={}", user.getEmail(), user.getId());
        } catch (Exception e) {
            log.warn("Failed to update Keycloak for sub={}, createdNewUser={}, err={}", sub, created, e.toString());
            // Trigger transaction rollback of any new insert
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak update failed", e);
        }
    }
}
