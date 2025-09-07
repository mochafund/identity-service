package com.beaver.identityservice.workspace.membership.service;

import com.beaver.identityservice.workspace.enums.PlanType;
import com.beaver.identityservice.workspace.enums.WorkspaceStatus;
import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.workspace.membership.enums.MembershipStatus;
import com.beaver.identityservice.workspace.membership.repository.IMembershipRepository;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.repository.IWorkspaceRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class MembershipService implements IMembershipService {

    private final IMembershipRepository membershipRepository;
    private final IWorkspaceRepository workspaceRepository;

    @Override
    @Transactional
    public WorkspaceMembership createDefaultMembership(User user, String name) {
        log.info("Creating workspace '{}' for user: {}", name, user.getId());

        List<WorkspaceMembership> userMemberships = this.getAllUserMemberships(user.getId());

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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkspaceMembership> getUserMembershipInWorkspace(UUID userId, UUID workspaceId) {
        log.debug("Getting membership for userId={} in workspaceId={}", userId, workspaceId);

        return membershipRepository.findByUserIdAndWorkspaceIdAndStatus(
                userId,
                workspaceId,
                MembershipStatus.ACTIVE
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMembership> getAllUserMemberships(UUID userId) {
        return membershipRepository.findAllByUserId(userId);
    }
}
