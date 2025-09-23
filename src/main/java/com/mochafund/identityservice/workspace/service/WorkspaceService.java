package com.mochafund.identityservice.workspace.service;

import com.mochafund.identityservice.common.exception.AccessDeniedException;
import com.mochafund.identityservice.common.exception.ResourceNotFoundException;
import com.mochafund.identityservice.kafka.KafkaProducer;
import com.mochafund.identityservice.keycloak.service.IKeycloakAdminService;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.repository.IUserRepository;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.enums.WorkspaceStatus;
import com.mochafund.identityservice.workspace.events.WorkspaceEvent;
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

    @Transactional
    public Workspace createWorkspace(UUID userId, CreateWorkspaceDto workspaceDto) {
        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .name(workspaceDto.getName())
                .status(WorkspaceStatus.PROVISIONING)
                .build());

        membershipService.createMembership(userId, workspace.getId(), Set.of(Role.OWNER, Role.WRITE, Role.READ));
        publishEvent("workspace.provisioning", workspace);

        return workspace;
    }

    @Transactional(readOnly = true)
    public Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId).orElseThrow(
                () -> new ResourceNotFoundException("Workspace not found"));
    }

    @Transactional
    public Workspace updateWorkspace(UUID workspaceId, UpdateWorkspaceDto workspaceDto) {
        log.info("Updating workspace with ID: {}", workspaceId);

        Workspace workspace = this.getWorkspace(workspaceId);
        workspace.patchFrom(workspaceDto);
        publishEvent("workspace.updated", workspace);

        return workspaceRepository.save(workspace);
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

        log.info("Successfully switched user {} to workspace '{}'", userId, targetWorkspace.getName());
        return targetWorkspace;
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
    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        log.info("Deleting workspace {} ({})", workspace.getName(), workspaceId);
        workspaceRepository.deleteById(workspaceId);

        WorkspaceEvent event = WorkspaceEvent.builder()
                .type("workspace.deleted.initialized")
                .data(WorkspaceEvent.Data.builder()
                        .workspaceId(workspace.getId())
                        .name(workspace.getName())
                        .status(workspace.getStatus().name())
                        .build())
                .build();

        kafkaProducer.send(event);
    }

    private void publishEvent(String type, Workspace workspace) {
        kafkaProducer.send(WorkspaceEvent.builder()
                .type(type)
                .data(WorkspaceEvent.Data.builder()
                        .workspaceId(workspace.getId())
                        .name(workspace.getName())
                        .status(workspace.getStatus().name())
                        .build())
                .build());
    }
}
