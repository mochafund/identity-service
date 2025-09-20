package com.mochafund.identityservice.workspace.controller;

import com.mochafund.identityservice.common.annotations.UserId;
import com.mochafund.identityservice.workspace.dto.CreateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.SwitchWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.WorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.service.IWorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WorkspaceDto>> getAllWorkspaces(@UserId UUID userId) {
        List<Workspace> workspaces = workspaceService.listAllByUserId(userId);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntities(workspaces));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> createWorkspace(
            @UserId UUID userId, @Valid @RequestBody CreateWorkspaceDto workspaceDto
    ) {
        Workspace createdWorkspace = workspaceService.createWorkspace(userId, workspaceDto);
        Workspace newWorkspace = workspaceService.switchWorkspace(userId, createdWorkspace.getId());
        return ResponseEntity.status(201).body(WorkspaceDto.fromEntity(newWorkspace));
    }

    @PostMapping(value = "/switch", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> switchWorkspace(
            @UserId UUID userId, @Valid @RequestBody SwitchWorkspaceDto switchWorkspaceDto
    ) {
        Workspace workspace = workspaceService.switchWorkspace(userId, switchWorkspaceDto.getWorkspaceId());
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(workspace));
    }
}
