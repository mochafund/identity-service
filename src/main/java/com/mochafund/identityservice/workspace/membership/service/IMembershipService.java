package com.mochafund.identityservice.workspace.membership.service;

import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.workspace.membership.dto.MembershipManagementDto;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IMembershipService {
    WorkspaceMembership createMembership(UUID userId, UUID workspaceId, Set<Role> roles);
    WorkspaceMembership updateMembership(UUID userId, UUID workspaceId, MembershipManagementDto membershipDto);
    void deleteMembership(UUID userId, UUID workspaceId, boolean force);
    List<WorkspaceMembership> listAllUserMemberships(UUID userId);
    List<WorkspaceMembership> listAllWorkspaceMemberships(UUID workspaceId);
}
