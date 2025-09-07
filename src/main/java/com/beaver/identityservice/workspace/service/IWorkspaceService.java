package com.beaver.identityservice.workspace.service;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;

import java.util.List;
import java.util.UUID;

public interface IWorkspaceService {
    WorkspaceMembership createWorkspace(User user, String name);
    List<Workspace> getAllByUserId(UUID userId);
    Workspace getById(UUID workspaceId);
    Workspace updateById(UUID workspaceId, UpdateWorkspaceDto workspaceDto);
}
