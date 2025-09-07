package com.beaver.identityservice.workspace.service;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.service.IUserService;
import com.beaver.identityservice.workspace.dto.CreateWorkspaceDto;
import com.beaver.identityservice.workspace.dto.SwitchWorkspaceDto;
import com.beaver.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.workspace.membership.service.IMembershipService;
import com.beaver.identityservice.workspace.repository.IWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkspaceService implements IWorkspaceService {

    private final IWorkspaceRepository workspaceRepository;
    private final IMembershipService membershipService;
    private final IUserService userService;

    @Override
    @Transactional
    public Workspace createWorkspace(UUID userId, CreateWorkspaceDto workspaceDto) {
        User user = userService.getById(userId);
        WorkspaceMembership workspaceMembership = membershipService
                .createDefaultMembership(user, workspaceDto.getName());
        return workspaceMembership.getWorkspace();
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

        Workspace workspace = this.getById(workspaceId);
        workspace.patchFrom(workspaceDto);

        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public Workspace switchWorkspace(UUID userId, UUID subject, SwitchWorkspaceDto switchWorkspaceDto) {
        log.info("User {} switching to workspace {}", userId, switchWorkspaceDto.getWorkspaceId());

        WorkspaceMembership membership = membershipService
                .getUserMembershipInWorkspace(userId, switchWorkspaceDto.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("User does not have access to workspace"));
        Workspace targetWorkspace = membership.getWorkspace();

        User user = userService.getById(userId);
        user.setLastWorkspaceId(targetWorkspace.getId());
        userService.save(user);
        userService.syncKeycloakUser(subject.toString(), user);

        log.info("Successfully switched user {} to workspace '{}'", userId, targetWorkspace.getName());
        return targetWorkspace;
    }
}
