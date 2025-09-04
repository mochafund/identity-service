package com.beaver.identityservice.role.service;

import com.beaver.identityservice.role.entity.WorkspaceRole;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.workspace.entity.Workspace;

import java.util.Optional;
import java.util.UUID;

public interface IWorkspaceRoleService {
    void createDefaultRoles(UUID workspaceId, Workspace workspace);
    Optional<WorkspaceRole> findByWorkspaceIdAndRoleType(UUID workspaceId, Role roleType);
}