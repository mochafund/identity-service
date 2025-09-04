package com.beaver.identityservice.workspace.service;

import com.beaver.identityservice.membership.service.IMembershipService;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.role.service.IWorkspaceRoleService;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.enums.PlanType;
import com.beaver.identityservice.workspace.enums.WorkspaceStatus;
import com.beaver.identityservice.workspace.repository.IWorkspaceRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class WorkspaceService implements IWorkspaceService {

    private final IWorkspaceRepository workspaceRepository;
    private final IMembershipService membershipService;
    private final IWorkspaceRoleService roleService;

    public WorkspaceMembership createDefaultWorkspace(User user) {
        log.info("Creating default workspace for user: {}", user.getId());

        String workspaceName = user.getName() + "'s Workspace";
        Workspace workspace = Workspace.builder()
                .name(workspaceName)
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created default workspace with ID: {}", workspace.getId());

        roleService.createDefaultRoles(workspace.getId(), workspace);

        WorkspaceMembership membership = membershipService.addUserToWorkspace(user, workspace, Role.OWNER);
        log.info("Added user {} as owner of default workspace {} with membership {}",
                user.getId(), workspace.getId(), membership.getId());

        return membership;
    }
}
