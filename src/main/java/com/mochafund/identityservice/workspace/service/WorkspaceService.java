package com.mochafund.identityservice.workspace.service;

import com.mochafund.identityservice.common.exception.AccessDeniedException;
import com.mochafund.identityservice.common.exception.ResourceNotFoundException;
import com.mochafund.identityservice.common.events.EventEnvelope;
import com.mochafund.identityservice.common.events.EventType;
import com.mochafund.identityservice.kafka.KafkaProducer;
import com.mochafund.identityservice.keycloak.service.IKeycloakAdminService;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.repository.IUserRepository;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.enums.WorkspaceStatus;
import com.mochafund.identityservice.workspace.events.WorkspaceEventPayload;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import com.mochafund.identityservice.workspace.repository.IWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkspaceService implements IWorkspaceService {

    private final IWorkspaceRepository workspaceRepository;
    private final IMembershipService membershipService;
    private final IUserRepository userRepository;
    private final IKeycloakAdminService keycloakAdminService;
    private final KafkaProducer kafkaProducer;

    @Transactional(readOnly = true)
    public Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId).orElseThrow(
                () -> new ResourceNotFoundException("Workspace not found"));
    }

    @Transactional(readOnly = true)
    public List<Workspace> listAllByUserId(UUID userId) {
        return membershipService.listAllUserMemberships(userId)
                .stream().map(WorkspaceMembership::getWorkspace).toList();
    }

    @Transactional(readOnly = true)
    public List<User> listAllMembers(UUID workspaceId) {
        return this.getWorkspace(workspaceId).getMemberships()
                .stream()
                .map(WorkspaceMembership::getUser)
                .toList();
    }

    @Transactional
    public Workspace provisionWorkspace(UUID userId, CreateWorkspaceDto workspaceDto) {
        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .status(WorkspaceStatus.PROVISIONING)
                .build());

        membershipService.createMembership(userId, workspace.getId(), Set.of(Role.OWNER, Role.WRITE, Role.READ));

        WorkspaceEventPayload payload = WorkspaceEventPayload.builder()
                .workspaceId(workspace.getId())
                .name(workspaceDto.getName())
                .status(workspace.getStatus().name())
                .build();

        kafkaProducer.send(EventEnvelope.<WorkspaceEventPayload>builder()
                .type(EventType.WORKSPACE_PROVISIONING)
                .payload(payload)
                .build());

        return workspace;
    }

    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        log.info("Deleting workspace {}", workspaceId);
        workspaceRepository.deleteById(workspaceId);

        WorkspaceEventPayload deletedPayload = WorkspaceEventPayload.builder()
                .workspaceId(workspace.getId())
                .status(workspace.getStatus().name())
                .build();

        kafkaProducer.send(EventEnvelope.<WorkspaceEventPayload>builder()
                .type(EventType.WORKSPACE_DELETED_INITIALIZED)
                .payload(deletedPayload)
                .build());
    }

    @Transactional
    public Workspace switchWorkspace(UUID userId, UUID workspaceId) {
        log.info("User {} switching to workspace {}", userId, workspaceId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkspaceMembership membership = user
                .getMemberships()
                .stream()
                .filter(w -> w.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("User does not have access to workspace"));

        Workspace targetWorkspace = membership.getWorkspace();
        user.setLastWorkspaceId(targetWorkspace.getId());
        userRepository.save(user);
        keycloakAdminService.syncAttributes(user);

        log.info("Successfully switched user {} to workspace '{}'", userId, targetWorkspace.getId());
        return targetWorkspace;
    }
}
