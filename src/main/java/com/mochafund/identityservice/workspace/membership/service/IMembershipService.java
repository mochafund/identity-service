package com.mochafund.identityservice.workspace.membership.service;

import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.workspace.dto.MembershipManagementDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IMembershipService {
    WorkspaceMembership createDefaultMembership(User user, String name);
    WorkspaceMembership addUserToWorkspace(User user, Workspace workspace, Set<Role> roles);
    WorkspaceMembership updateMembership(UUID userId, UUID workspaceId, MembershipManagementDto membershipDto);
    List<WorkspaceMembership> listAllWorkspaceMemberships(UUID workspaceId);
    List<WorkspaceMembership> listAllUserMemberships(UUID userId);
    int deleteByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
}
