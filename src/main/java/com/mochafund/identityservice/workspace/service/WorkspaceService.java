package com.mochafund.identityservice.workspace.service;

import com.mochafund.identityservice.keycloak.service.IKeycloakAdminService;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.service.IUserService;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.enums.PlanType;
import com.mochafund.identityservice.workspace.enums.WorkspaceStatus;
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
    private final IUserService userService;
    private final IKeycloakAdminService keycloakAdminService;

    @Transactional
    public Workspace createWorkspace(UUID userId, CreateWorkspaceDto workspaceDto) {
        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .name(workspaceDto.getName())
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build());

        membershipService.createMembership(userId, workspace.getId(), Set.of(Role.OWNER, Role.WRITE, Role.READ));

        return workspace;
    }

    @Transactional(readOnly = true)
    public Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId).orElseThrow(
                () -> new IllegalArgumentException("Workspace not found"));
    }

    @Transactional
    public Workspace updateWorkspace(UUID workspaceId, UpdateWorkspaceDto workspaceDto) {
        log.info("Updating workspace with ID: {}", workspaceId);

        Workspace workspace = this.getWorkspace(workspaceId);
        workspace.patchFrom(workspaceDto);

        return workspaceRepository.save(workspace);
    }

    @Transactional
    public void leaveWorkspace(UUID userId, UUID workspaceId) {
        membershipService.deleteMembership(userId, workspaceId);
        User user = userService.getUser(userId);

        if (workspaceId.equals(user.getLastWorkspaceId())) {
            WorkspaceMembership next = membershipService.listAllUserMemberships(userId).getFirst();
            user.setLastWorkspaceId(next.getWorkspace().getId());
            userService.save(user);
            keycloakAdminService.syncAttributes(user);
        }
    }

    @Transactional
    public Workspace switchWorkspace(UUID userId, UUID workspaceId) {
        log.info("User {} switching to workspace {}", userId, workspaceId);

        WorkspaceMembership membership = membershipService
                .listAllUserMemberships(userId)
                .stream()
                .filter(w -> w.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User does not have access to workspace"));
        Workspace targetWorkspace = membership.getWorkspace();

        User user = userService.getUser(userId);
        user.setLastWorkspaceId(targetWorkspace.getId());
        userService.save(user);
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
}
