package com.beaver.identityservice.workspace.service;

import com.beaver.identityservice.workspace.dto.SwitchWorkspaceDto;
import com.beaver.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.beaver.identityservice.workspace.entity.Workspace;

import java.util.List;
import java.util.UUID;

public interface IWorkspaceService {
    List<Workspace> getAllByUserId(UUID userId);
    Workspace getById(UUID workspaceId);
    Workspace updateById(UUID workspaceId, UpdateWorkspaceDto workspaceDto);
    Workspace switchWorkspace(UUID userId, UUID subject, SwitchWorkspaceDto switchWorkspaceDto);
}
