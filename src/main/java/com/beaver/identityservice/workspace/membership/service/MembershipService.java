package com.beaver.identityservice.workspace.membership.service;

import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.workspace.membership.enums.MembershipStatus;
import com.beaver.identityservice.workspace.membership.role.IMembershipRepository;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.entity.Workspace;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class MembershipService implements IMembershipService {

    private final IMembershipRepository membershipRepository;

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

    public Optional<WorkspaceMembership> getUserMembershipInWorkspace(UUID userId, UUID workspaceId) {
        log.debug("Getting membership for userId={} in workspaceId={}", userId, workspaceId);

        return membershipRepository.findByUserIdAndWorkspaceIdAndStatus(
                userId,
                workspaceId,
                MembershipStatus.ACTIVE
        );
    }
}
