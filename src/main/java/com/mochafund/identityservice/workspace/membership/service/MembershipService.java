package com.mochafund.identityservice.workspace.membership.service;

import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.service.IUserService;
import com.mochafund.identityservice.workspace.dto.MembershipManagementDto;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import com.mochafund.identityservice.workspace.membership.repository.IMembershipRepository;
import com.mochafund.identityservice.workspace.service.IWorkspaceService;
import jakarta.ws.rs.NotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class MembershipService implements IMembershipService {

    private final IMembershipRepository membershipRepository;
    private final IWorkspaceService workspaceService;
    private final IUserService userService;

    @Transactional
    public WorkspaceMembership createMembership(UUID userId, UUID workspaceId, Set<Role> roles) {
        log.info("Adding user {} to workspace {} with roles {}", userId, workspaceId, roles);
        Workspace workspace = workspaceService.getWorkspace(workspaceId);
        User user = userService.getUser(userId);

        membershipRepository.findAllByUser_Id(userId)
                .stream()
                .filter(w -> w.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .ifPresent(membership -> {
                    throw new IllegalArgumentException("User already has membership to this workspace");
                });

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .user(user)
                .workspace(workspace)
                .roles(roles)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        return membershipRepository.save(membership);
    }

    @Transactional
    public WorkspaceMembership updateMembership(UUID userId, UUID workspaceId, MembershipManagementDto membershipDto) {
        log.info("Updating membership with ID: {}", workspaceId);

        WorkspaceMembership membership = membershipRepository
                .findAllByUser_Id(userId)
                .stream()
                .filter(w -> w.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User does not have a membership to workspace"));
        membership.patchFrom(membershipDto);

        return membershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMembership> listAllUserMemberships(UUID userId) {
        return membershipRepository.findAllByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMembership> listAllWorkspaceMemberships(UUID workspaceId) {
        return membershipRepository.findAllByWorkspace_Id(workspaceId);
    }

    @Transactional
    public void deleteMembership(UUID userId, UUID workspaceId) {
        membershipRepository.findAllByUser_Id(userId)
                .stream()
                .filter(w -> w.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User does not have a membership to workspace"));

        long total = membershipRepository.countByUserId(userId);
        if (total <= 1) {
            // TODO: Handle this case better... maybe we can create our own exception and global handler
            throw new NotAllowedException("User can't be removed from their only workspace.");
        }

        // TODO: Publish MembershipDeleted event (userId, workspaceId) AFTER COMMIT to clean up orphaned workspace
        membershipRepository.deleteByUserIdAndWorkspaceId(userId, workspaceId);
    }
}
