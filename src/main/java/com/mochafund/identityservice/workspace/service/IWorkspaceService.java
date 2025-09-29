package com.mochafund.identityservice.workspace.service;

import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;

import java.util.List;
import java.util.UUID;

public interface IWorkspaceService {
    Workspace getWorkspace(UUID workspaceId);
    List<Workspace> listAllByUserId(UUID userId);
    List<User> listAllMembers(UUID workspaceId);
    Workspace provisionWorkspace(UUID userId, CreateWorkspaceDto workspaceDto);
    void deleteWorkspace(UUID workspaceId);
    Workspace switchWorkspace(UUID userId, UUID workspaceId);
}
