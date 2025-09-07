package com.beaver.identityservice.workspace.controller;

import com.beaver.identityservice.common.annotations.UserId;
import com.beaver.identityservice.common.annotations.WorkspaceId;
import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.user.service.IUserService;
import com.beaver.identityservice.workspace.dto.CreateWorkspaceDto;
import com.beaver.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.beaver.identityservice.workspace.dto.WorkspaceDto;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.beaver.identityservice.workspace.service.IWorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final IWorkspaceService workspaceService;
    private final IUserService userService;

    // TODO: Switch current workspace immediately after creating a new workspace
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> createWorkspace(
            @UserId UUID userId,
            @Valid @RequestBody CreateWorkspaceDto workspaceDto) {
        User user = userService.getById(userId);
        WorkspaceMembership workspaceMembership = workspaceService.createWorkspace(user, workspaceDto.getName());
        Workspace workspace = workspaceMembership.getWorkspace();
        return ResponseEntity.status(201).body(WorkspaceDto.fromEntity(workspace));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WorkspaceDto>> getAllWorkspaces(@UserId UUID userId) {
        List<Workspace> workspaces = workspaceService.getAllByUserId(userId);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntities(workspaces));
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> getCurrentWorkspace(@WorkspaceId UUID workspaceId) {
        Workspace workspace = workspaceService.getById(workspaceId);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(workspace));
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @PatchMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> updateCurrentWorkspace(
            @WorkspaceId UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceDto updateDto) {
        Workspace updatedWorkspace = workspaceService.updateById(workspaceId, updateDto);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(updatedWorkspace));
    }

    // TODO: DELETE workspace
}
