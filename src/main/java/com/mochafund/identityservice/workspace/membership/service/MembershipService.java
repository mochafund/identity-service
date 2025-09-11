package com.mochafund.identityservice.workspace.membership.service;

import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.workspace.dto.MembershipManagementDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.enums.PlanType;
import com.mochafund.identityservice.workspace.enums.WorkspaceStatus;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import com.mochafund.identityservice.workspace.membership.repository.IMembershipRepository;
import com.mochafund.identityservice.workspace.repository.IWorkspaceRepository;
import jakarta.ws.rs.NotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class MembershipService implements IMembershipService {

    private final IMembershipRepository membershipRepository;
    private final IWorkspaceRepository workspaceRepository;

    @Transactional
    public WorkspaceMembership createDefaultMembership(User user, String name) {
        log.info("Creating workspace '{}' for user: {}", name, user.getId());

        List<WorkspaceMembership> userMemberships = membershipRepository.findAllByUser_Id(user.getId());

        Set<String> existingNames = userMemberships.stream()
                .map(WorkspaceMembership::getWorkspace)
                .map(Workspace::getName)
                .collect(Collectors.toSet());

        String finalName = name;
        if (existingNames.contains(name)) {
            int counter = 1;
            do {
                finalName = name + " (" + counter + ")";
                counter++;
            } while (existingNames.contains(finalName));
        }

        Workspace workspace = Workspace.builder()
                .name(finalName)
                .status(WorkspaceStatus.ACTIVE)
                .plan(PlanType.STARTER)
                .build();

        workspace = workspaceRepository.save(workspace);
        log.info("Created workspace '{}' with ID: {}", finalName, workspace.getId());

        WorkspaceMembership membership = this.addUserToWorkspace(
                user, workspace, Set.of(Role.READ, Role.WRITE, Role.OWNER)
        );
        log.info("Added user {} as owner of workspace '{}' with membership {}",
                user.getId(), finalName, membership.getId());

        return membership;
    }

    @Transactional
    public WorkspaceMembership addUserToWorkspace(User user, Workspace workspace, Set<Role> roles) {
        log.info("Adding user {} to workspace {} with roles {}", user.getId(), workspace.getId(), roles);

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .user(user)
                .workspace(workspace)
                .roles(roles)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        return membershipRepository.save(membership);
    }

    @Transactional
    public WorkspaceMembership updateMembership(UUID userId, UUID workspaceId, MembershipManagementDto membershipDto) {
        log.info("Updating membership with ID: {}", workspaceId);

        WorkspaceMembership membership = membershipRepository
                .findAllByUser_Id(userId)
                .stream()
                .filter(w -> w.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User does not have a membership to workspace"));
        membership.patchFrom(membershipDto);

        return membershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMembership> listAllUserMemberships(UUID userId) {
        return membershipRepository.findAllByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMembership> listAllWorkspaceMemberships(UUID workspaceId) {
        return membershipRepository.findAllByWorkspace_Id(workspaceId);
    }

    @Transactional
    public int deleteByUserIdAndWorkspaceId(UUID userId, UUID workspaceId) {
        long total = membershipRepository.countByUserId(userId);
        if (total <= 1) {
            // TODO: Handle this case better... maybe we can create our own exception and global handler
            throw new NotAllowedException("User can't be removed from their only workspace.");
        }

        // TODO: Publish MembershipDeleted event (userId, workspaceId) AFTER COMMIT to clean up orphaned workspace
        return membershipRepository.deleteByUserIdAndWorkspaceId(userId, workspaceId);
    }
}
