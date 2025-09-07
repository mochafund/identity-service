package com.beaver.identityservice.workspace.controller;

import com.beaver.identityservice.common.annotations.UserId;
import com.beaver.identityservice.common.annotations.WorkspaceId;
import com.beaver.identityservice.workspace.dto.WorkspaceDto;
import com.beaver.identityservice.workspace.entity.Workspace;
import com.beaver.identityservice.workspace.service.IWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
        List<Workspace> workspaces = workspaceService.getAllByUserId(userId);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntities(workspaces));
    }

    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> getCurrentWorkspace(@WorkspaceId UUID workspaceId) {
        Workspace workspace = workspaceService.getById(workspaceId);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(workspace));
    }
}
