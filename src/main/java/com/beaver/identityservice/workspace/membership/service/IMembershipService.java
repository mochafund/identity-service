package com.beaver.identityservice.workspace.membership.service;

import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.entity.Workspace;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IMembershipService {
    WorkspaceMembership createDefaultMembership(User user, String name);
    WorkspaceMembership addUserToWorkspace(User user, Workspace workspace, Set<Role> roles);
    Optional<WorkspaceMembership> getUserMembershipInWorkspace(UUID userId, UUID workspaceId);
    List<WorkspaceMembership> getAllUserMemberships(UUID userId);
    long countMembershipsForUser(UUID userId);
    int deleteByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
}
