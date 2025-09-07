package com.beaver.identityservice.workspace.service;

import com.beaver.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.beaver.identityservice.workspace.membership.service.IMembershipService;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.enums.PlanType;
import com.beaver.identityservice.workspace.enums.WorkspaceStatus;
import com.beaver.identityservice.workspace.repository.IWorkspaceRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkspaceService implements IWorkspaceService {

    private final IWorkspaceRepository workspaceRepository;
    private final IMembershipService membershipService;

    @Override
    @Transactional
    public WorkspaceMembership createDefaultWorkspace(User user) {
        log.info("Creating default workspace for user: {}", user.getId());

        Workspace workspace = Workspace.builder()
                .name(user.getName() + "'s Workspace")
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created default workspace with ID: {}", workspace.getId());

        WorkspaceMembership membership = membershipService
                .addUserToWorkspace(user, workspace, Set.of(Role.READ, Role.WRITE, Role.OWNER));
        log.info("Added user {} as owner of default workspace {} with membership {}",
                user.getId(), workspace.getId(), membership.getId());

        return membership;
    }

    @Override
    @Transactional(readOnly = true)
    public Workspace getById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId).orElseThrow(
                () -> new IllegalArgumentException("Workspace not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> getAllByUserId(UUID userId) {
        return membershipService.getAllUserMemberships(userId)
                .stream().map(WorkspaceMembership::getWorkspace).toList();
    }

    @Override
    @Transactional
    public Workspace updateById(UUID workspaceId, UpdateWorkspaceDto workspaceDto) {
        log.info("Updating workspace with ID: {}", workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        workspace.setName(workspaceDto.getName());

        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        log.info("Successfully updated workspace {} with new name: {}",
                workspaceId, workspaceDto.getName());

        return updatedWorkspace;
    }
}
