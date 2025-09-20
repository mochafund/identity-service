package com.mochafund.identityservice.user.service;

import com.mochafund.identityservice.common.exception.BadRequestException;
import com.mochafund.identityservice.common.exception.ConflictException;
import com.mochafund.identityservice.common.exception.InternalServerException;
import com.mochafund.identityservice.common.exception.ResourceNotFoundException;
import com.mochafund.identityservice.kafka.KafkaProducer;
import com.mochafund.identityservice.keycloak.service.IKeycloakAdminService;
import com.mochafund.identityservice.user.dto.UpdateUserDto;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.events.UserEvent;
import com.mochafund.identityservice.user.repository.IUserRepository;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.service.IWorkspaceService;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IKeycloakAdminService keycloakAdminService;
    private final IWorkspaceService workspaceService;
    private final IMembershipService membershipService;
    private final KafkaProducer kafkaProducer;

    @Transactional(readOnly = true)
    public User getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User updateUser(UUID userId, UpdateUserDto userDto) {
        log.info("Updating user with ID: {}", userId);

        User user = this.getUser(userId);
        String oldEmail = user.getEmail();

        boolean invalidate = false;
        if (userDto.getEmail() != null) {
            invalidate = true;
            userRepository.findByEmail(userDto.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(userId)) {
                            throw new ConflictException("Email already in use");
                        }
                    });
        }

        user.patchFrom(userDto);
        User updatedUser = userRepository.save(user);
        keycloakAdminService.syncAttributes(updatedUser);
        publishEventWithOldEmail("user.updated", updatedUser, oldEmail, invalidate);

        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var memberships = membershipService.listAllUserMemberships(userId);
        log.info("User {} has {} workspace memberships to delete", user.getEmail(), memberships.size());
        
        for (WorkspaceMembership membership : memberships) {
            membershipService.deleteMembership(userId, membership.getWorkspace().getId(), true);
        }

        try {
            keycloakAdminService.logoutAllSessions();
            keycloakAdminService.deleteUser();
            userRepository.deleteById(userId);

            log.info("Successfully deleted user {}", user.getEmail());
            publishEvent("user.deleted", user, true);

        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", user.getEmail(), e.getMessage());
            throw new InternalServerException(e.getMessage());
        }
    }

    @Transactional
    public User createUser(Jwt jwt) {
        final String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new BadRequestException("JWT missing subject claim");
        }
        final String email = Optional.ofNullable(jwt.getClaimAsString("email"))
                .orElseThrow(() -> new BadRequestException("JWT missing email"));
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
                        .orElseThrow(() -> new ConflictException("User just created but not found"));
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
                log.error("Failed to create default workspace for user {}", user.getId());
                throw new InternalServerException(e.getMessage());
            }
        }

        try {
            keycloakAdminService.syncAttributes(sub, user);
            log.debug("Keycloak attributes set for email={}, userId={}", user.getEmail(), user.getId());
            publishEvent("user.created", user, false);
            return user;
        } catch (Exception e) {
            log.warn("Failed to update Keycloak for sub={}, createdNewUser={}, err={}", sub, created, e.toString());
            throw new InternalServerException(e.getMessage());
        }
    }

    private void publishEvent(String type, User user, boolean invalidate) {
        publishEventWithOldEmail(type, user, null, invalidate);
    }

    private void publishEventWithOldEmail(String type, User user, String oldEmail, boolean invalidate) {
        kafkaProducer.send(
                UserEvent.builder()
                        .type(type)
                        .data(UserEvent.Data.builder()
                                .userId(user.getId())
                                .email(user.getEmail())
                                .oldEmail(oldEmail)
                                .givenName(user.getGivenName())
                                .familyName(user.getFamilyName())
                                .isActive(user.getIsActive())
                                .lastWorkspaceId(user.getLastWorkspaceId())
                                .invalidate(invalidate)
                                .build())
                        .build()
        );
    }
}
