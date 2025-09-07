package com.beaver.identityservice.workspace.service;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;

import java.util.UUID;

public interface IWorkspaceService {
    WorkspaceMembership createDefaultWorkspace(User user);
    Workspace getById(UUID workspaceId);
}
