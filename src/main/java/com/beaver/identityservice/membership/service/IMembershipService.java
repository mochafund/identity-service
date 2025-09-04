package com.beaver.identityservice.membership.service;

import com.beaver.identityservice.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.entity.Workspace;

import java.util.Optional;
import java.util.UUID;

public interface IMembershipService {
    WorkspaceMembership addUserToWorkspace(User user, Workspace workspace, Role roleType);
    Optional<Role> getUserRoleInWorkspace(UUID userId, UUID workspaceId);
}
