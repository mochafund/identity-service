package com.mochafund.identityservice.workspace.controller;

import com.mochafund.identityservice.common.annotations.WorkspaceId;
import com.mochafund.identityservice.user.dto.UserDto;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.workspace.membership.dto.CreateMembershipDto;
import com.mochafund.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.WorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.membership.dto.UpdateMembershipDto;
import com.mochafund.identityservice.workspace.membership.dto.WorkspaceMembershipDto;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import com.mochafund.identityservice.workspace.service.IWorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/workspaces/current")
public class CurrentWorkspaceController {

    private final IMembershipService membershipService;
    private final IWorkspaceService workspaceService;

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> getCurrentWorkspace(@WorkspaceId UUID workspaceId) {
        Workspace workspace = workspaceService.getWorkspace(workspaceId);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(workspace));
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> updateCurrentWorkspace(
            @WorkspaceId UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceDto updateDto
    ) {
        Workspace updatedWorkspace = workspaceService.updateWorkspace(workspaceId, updateDto);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(updatedWorkspace));
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping(value = "/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getMembers(@WorkspaceId UUID workspaceId) {
        List<User> users = workspaceService.listAllMembers(workspaceId);
        return ResponseEntity.ok().body(UserDto.fromEntities(users));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PostMapping(value = "/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceMembershipDto> addWorkspaceMembership(
            @WorkspaceId UUID workspaceId,
            @Valid @RequestBody CreateMembershipDto membershipDto
    ) {
        WorkspaceMembership membership = membershipService
                .createMembership(membershipDto.getUserId(), workspaceId, membershipDto.getRoles());

        return ResponseEntity.ok().body(WorkspaceMembershipDto.fromEntity(membership));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PatchMapping(value = "/members/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceMembershipDto> updateWorkspaceMembership(
            @WorkspaceId UUID workspaceId, @PathVariable UUID userId,
            @Valid @RequestBody UpdateMembershipDto membershipDto
    ) {
        WorkspaceMembership membership = membershipService.updateMembership(userId, workspaceId, membershipDto);
        return ResponseEntity.ok().body(WorkspaceMembershipDto.fromEntity(membership));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @DeleteMapping(value = "/members/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteWorkspaceMembership(
            @WorkspaceId UUID workspaceId, @PathVariable UUID userId
    ) {
        membershipService.deleteMembership(userId, workspaceId, false);
        return ResponseEntity.noContent().build();
    }
}
