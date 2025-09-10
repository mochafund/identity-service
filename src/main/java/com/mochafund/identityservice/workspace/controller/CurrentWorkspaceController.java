package com.mochafund.identityservice.workspace.controller;

import com.mochafund.identityservice.common.annotations.Subject;
import com.mochafund.identityservice.common.annotations.UserId;
import com.mochafund.identityservice.common.annotations.WorkspaceId;
import com.mochafund.identityservice.user.dto.UserDto;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.service.IUserService;
import com.mochafund.identityservice.workspace.dto.MembershipManagementDto;
import com.mochafund.identityservice.workspace.dto.UpdateWorkspaceDto;
import com.mochafund.identityservice.workspace.dto.WorkspaceDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
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
    private final IUserService userService;

    // TODO: Update user's role in current workspace (OWNER)
    // TODO: Remove user from current workspace (OWNER)

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> getCurrentWorkspace(@WorkspaceId UUID workspaceId) {
        Workspace workspace = workspaceService.getById(workspaceId);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(workspace));
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceDto> updateCurrentWorkspace(
            @WorkspaceId UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceDto updateDto
    ) {
        Workspace updatedWorkspace = workspaceService.updateById(workspaceId, updateDto);
        return ResponseEntity.ok().body(WorkspaceDto.fromEntity(updatedWorkspace));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @DeleteMapping()
    public ResponseEntity<Void> deleteCurrentWorkspace(
            @UserId UUID userId, @Subject UUID subject,
            @WorkspaceId UUID workspaceId) {
        workspaceService.leaveWorkspace(userId, subject, workspaceId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping(value = "/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getMembers(@WorkspaceId UUID workspaceId) {
        List<User> users = workspaceService.getAllUsersInWorkspace(workspaceId);
        return ResponseEntity.ok().body(UserDto.fromEntities(users));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PostMapping(value = "/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceMembershipDto> addWorkspaceMembership(
            @WorkspaceId UUID workspaceId,
            @Valid @RequestBody MembershipManagementDto membershipDto
    ) {
        membershipService.getUserMembershipInWorkspace(membershipDto.getUserId(), workspaceId)
                .ifPresent(membership -> {
                    throw new IllegalArgumentException("User already has access to workspace");
                });

        User user = userService.getById(membershipDto.getUserId());
        Workspace workspace = workspaceService.getById(workspaceId);
        WorkspaceMembership membership = membershipService
                .addUserToWorkspace(user, workspace, membershipDto.getRoles());

        return ResponseEntity.ok().body(WorkspaceMembershipDto.fromEntity(membership));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PatchMapping(value = "/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkspaceMembershipDto> updateWorkspaceMembership(
            @WorkspaceId UUID workspaceId,
            @Valid @RequestBody MembershipManagementDto membershipDto
    ) {
        WorkspaceMembership membership = membershipService.updateMembership(workspaceId, membershipDto);
        return ResponseEntity.ok().body(WorkspaceMembershipDto.fromEntity(membership));
    }
}
