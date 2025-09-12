package com.mochafund.identityservice.workspace.service;

import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;

import java.util.List;
import java.util.UUID;

public interface IWorkspaceService {
    Workspace createWorkspace(UUID userId, CreateWorkspaceDto workspaceDto);
    Workspace updateWorkspace(UUID workspaceId, UpdateWorkspaceDto workspaceDto);
    Workspace getWorkspace(UUID workspaceId);
    Workspace switchWorkspace(UUID userId, UUID workspaceId);
    void leaveWorkspace(UUID userId, UUID workspaceId);
    List<Workspace> listAllByUserId(UUID userId);
    List<User> listAllMembers(UUID workspaceId);
}
