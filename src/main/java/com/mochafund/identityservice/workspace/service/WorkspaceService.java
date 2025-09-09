package com.mochafund.identityservice.workspace.service;

import com.mochafund.identityservice.keycloak.service.IKeycloakAdminService;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.service.IUserService;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.SwitchWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import com.mochafund.identityservice.workspace.repository.IWorkspaceRepository;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
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
    private final IKeycloakAdminService keycloakAdminService;

    @Transactional
    public Workspace createWorkspace(UUID userId, CreateWorkspaceDto workspaceDto) {
        User user = userService.getById(userId);
        WorkspaceMembership workspaceMembership = membershipService
                .createDefaultMembership(user, workspaceDto.getName());
        return workspaceMembership.getWorkspace();
    }

    @Transactional(readOnly = true)
    public Workspace getById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId).orElseThrow(
                () -> new IllegalArgumentException("Workspace not found"));
    }

    @Transactional(readOnly = true)
    public List<Workspace> getAllByUserId(UUID userId) {
        return membershipService.getAllUserMemberships(userId)
                .stream().map(WorkspaceMembership::getWorkspace).toList();
    }

    @Transactional
    public Workspace updateById(UUID workspaceId, UpdateWorkspaceDto workspaceDto) {
        log.info("Updating workspace with ID: {}", workspaceId);

        Workspace workspace = this.getById(workspaceId);
        workspace.patchFrom(workspaceDto);

        return workspaceRepository.save(workspace);
    }

    @Transactional
    public void leaveWorkspace(UUID userId, UUID subject, UUID workspaceId) {
        long total = membershipService.countMembershipsForUser(userId);
        if (total <= 1) {
            // TODO: Handle this case better... maybe we can create our own exception and global handler
            throw new NotAllowedException("You can't leave your only workspace.");
        }

        int deleted = membershipService.deleteByUserIdAndWorkspaceId(userId, workspaceId);
        if (deleted == 0) {
            throw new NotFoundException("Membership not found for this user and workspace.");
        }

        // TODO: Publish MembershipDeleted event (userId, workspaceId) AFTER COMMIT to clean up orphaned workspaces
        // e.g., applicationEventPublisher.publishEvent(new MembershipDeletedEvent(userId, workspaceId));

        User user = userService.getById(userId);
        if (workspaceId.equals(user.getLastWorkspaceId())) {
            WorkspaceMembership next = membershipService.getAllUserMemberships(userId).getFirst();
            user.setLastWorkspaceId(next.getWorkspace().getId());
            userService.save(user);
            keycloakAdminService.syncAttributes(subject.toString(), user);
        }
    }

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
        keycloakAdminService.syncAttributes(subject.toString(), user);

        log.info("Successfully switched user {} to workspace '{}'", userId, targetWorkspace.getName());
        return targetWorkspace;
    }
}
