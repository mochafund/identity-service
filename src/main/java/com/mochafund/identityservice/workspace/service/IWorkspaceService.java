package com.mochafund.identityservice.workspace.service;

import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.SwitchWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;

import java.util.List;
import java.util.UUID;

public interface IWorkspaceService {
    Workspace createWorkspace(UUID userId, CreateWorkspaceDto workspaceDto);
    List<Workspace> getAllByUserId(UUID userId);
    Workspace getById(UUID workspaceId);
    Workspace updateById(UUID workspaceId, UpdateWorkspaceDto workspaceDto);
    void leaveWorkspace(UUID userId, UUID subject, UUID workspaceId);
    Workspace switchWorkspace(UUID userId, UUID subject, UUID workspaceId);
}
