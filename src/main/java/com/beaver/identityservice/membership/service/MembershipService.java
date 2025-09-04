package com.beaver.identityservice.membership.service;

import com.beaver.identityservice.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.membership.enums.MembershipStatus;
import com.beaver.identityservice.membership.role.IMembershipRepository;
import com.beaver.identityservice.role.entity.WorkspaceRole;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.role.service.IWorkspaceRoleService;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.entity.Workspace;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class MembershipService implements IMembershipService {

    private final IMembershipRepository membershipRepository;
    private final IWorkspaceRoleService roleService;

    public WorkspaceMembership addUserToWorkspace(User user, Workspace workspace, Role roleType) {
        log.info("Adding user {} to workspace {} with role {}", user.getId(), workspace.getId(), roleType);

        WorkspaceRole role = roleService.findByWorkspaceIdAndRoleType(workspace.getId(), roleType)
                .orElseThrow(() -> new IllegalStateException(
                        "Role " + roleType + " not found for workspace " + workspace.getId() +
                                ". This should not happen as all roles should be created during workspace setup."));

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .user(user)
                .workspace(workspace)
                .role(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        return membershipRepository.save(membership);
    }
}
